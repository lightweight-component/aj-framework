package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.oauth.enums.AuthResponseStatus;
import com.ajaxjs.oauth.enums.AuthUserGender;
import com.ajaxjs.oauth.enums.scope.AuthHuaweiV3Scope;
import com.ajaxjs.oauth.model.AuthException;
import com.ajaxjs.oauth.utils.*;
import com.ajaxjs.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.xkcoding.http.constants.Constants;
import com.xkcoding.http.support.HttpHeader;
import com.ajaxjs.oauth.model.AuthCallback;
import com.ajaxjs.oauth.model.AuthResponse;
import com.ajaxjs.oauth.model.AuthToken;
import com.ajaxjs.oauth.model.AuthUser;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 华为授权登录
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @version 1.0
 * @since 1.16.7
 */
public class AuthHuaweiV3Request extends AuthDefaultRequest {

    public AuthHuaweiV3Request(AuthConfig config) {
        super(config, AuthDefaultSource.HUAWEI_V3);
    }

    public AuthHuaweiV3Request(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.HUAWEI_V3, authStateCache);
    }

    /**
     * 获取access token
     *
     * @param authCallback 授权成功后的回调参数
     * @return token
     * @see AuthDefaultRequest#authorize()
     * @see AuthDefaultRequest#authorize(String)
     */
    @Override
    public AuthToken getAccessToken(AuthCallback authCallback) {
        Map<String, String> form = new HashMap<>(8);
        form.put("grant_type", "authorization_code");
        form.put("code", authCallback.getCode());
        form.put("client_id", config.getClientId());
        form.put("client_secret", config.getClientSecret());
        form.put("redirect_uri", config.getRedirectUri());

        if (config.isPkce()) {
            String cacheKey = this.source.getName().concat(":code_verifier:").concat(authCallback.getState());
            String codeVerifier = this.authStateCache.get(cacheKey);
            form.put("code_verifier", codeVerifier);
        }

        HttpHeader httpHeader = new HttpHeader();
        httpHeader.add(Constants.CONTENT_TYPE, HttpConstants.CONTENT_TYPE_FORM
);
        String response = new HttpUtils(config.getHttpConfig()).post(source.accessToken(), form, httpHeader, false).getBody();
        return getAuthToken(response);
    }

    /**
     * 使用token换取用户信息
     *
     * @param authToken token信息
     * @return 用户信息
     * @see AuthDefaultRequest#getAccessToken(AuthCallback)
     */
    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        String idToken = authToken.getIdToken();

        if (StrUtil.isEmptyTextText(idToken)) {
            Map<String, String> form = new HashMap<>(7);
            form.put("access_token", authToken.getAccessToken());
            form.put("getNickName", "1");
            form.put("nsp_svc", "GOpen.User.getInfo");

            HttpHeader httpHeader = new HttpHeader();
            httpHeader.add(Constants.CONTENT_TYPE, HttpConstants.CONTENT_TYPE_FORM
);
            String response = new HttpUtils(config.getHttpConfig()).post(source.userInfo(), form, httpHeader, false).getBody();
            JSONObject object = JSONObject.parseObject(response);

            this.checkResponse(object);

            return AuthUser.builder()
                .rawUserInfo(object)
                .uuid(object.getString("unionID"))
                .username(object.getString("displayName"))
                .nickname(object.getString("displayName"))
                .gender(AuthUserGender.UNKNOWN)
                .avatar(object.getString("headPictureURL"))
                .token(authToken)
                .source(source.toString())
                .build();
        }
        String payload = new String(Base64.getUrlDecoder().decode(idToken.split("\\.")[1]), StandardCharsets.UTF_8);

        JSONObject object = JSONObject.parseObject(payload);
        return AuthUser.builder()
            .rawUserInfo(object)
            .uuid(object.getString("sub"))
            .username(object.getString("name"))
            .nickname(object.getString("nickname"))
            .gender(AuthUserGender.UNKNOWN)
            .avatar(object.getString("picture"))
            .token(authToken)
            .source(source.toString())
            .build();
    }

    /**
     * 刷新access token （续期）
     *
     * @param authToken 登录成功后返回的Token信息
     * @return AuthResponse
     */
    @Override
    public AuthResponse<AuthToken> refresh(AuthToken authToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("client_id", config.getClientId());
        form.put("client_secret", config.getClientSecret());
        form.put("refresh_token", authToken.getRefreshToken());
        form.put("grant_type", "refresh_token");

        HttpHeader httpHeader = new HttpHeader();
        httpHeader.add(Constants.CONTENT_TYPE, HttpConstants.CONTENT_TYPE_FORM
);
        String response = new HttpUtils(config.getHttpConfig()).post(source.refresh(), form, httpHeader, false).getBody();
        return AuthResponse.<AuthToken>builder().code(AuthResponseStatus.SUCCESS.getCode()).data(getAuthToken(response)).build();
    }

    private AuthToken getAuthToken(String response) {
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);

        return AuthToken.builder()
            .accessToken(object.getString("access_token"))
            .expireIn(object.getIntValue("expires_in"))
            .refreshToken(object.getString("refresh_token"))
            .idToken(object.getString("id_token"))
            .build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     * @since 1.9.3
     */
    @Override
    public String authorize(String state) {
        String realState = getRealState(state);
        UrlBuilder builder = UrlBuilder.fromBaseUrl(super.authorize(realState))
            .queryParam("access_type", "offline")
            .queryParam("scope", this.getScopes(" ", true, AuthChecker.getDefaultScopes(AuthHuaweiV3Scope.values())));

        if (config.isPkce()) {
            String cacheKey = this.source.getName().concat(":code_verifier:").concat(realState);
            String codeVerifier = AuthChecker.generateCodeVerifier();
            String codeChallengeMethod = "S256";
            String codeChallenge = AuthChecker.generateCodeChallenge(codeChallengeMethod, codeVerifier);
            builder.queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", codeChallengeMethod);
            // 缓存 codeVerifier 十分钟
            this.authStateCache.cache(cacheKey, codeVerifier, TimeUnit.MINUTES.toMillis(10));
        }
        return builder.build();
    }

    /**
     * 校验响应结果
     *
     * @param object 接口返回的结果
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("NSP_STATUS")) {
            throw new AuthException(object.getString("error"));
        }
        if (object.containsKey("error")) {
            throw new AuthException(object.getString("sub_error") + ":" + object.getString("error_description"));
        }
    }


}
