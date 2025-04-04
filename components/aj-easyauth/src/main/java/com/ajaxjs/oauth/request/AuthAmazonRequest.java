package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.enums.AuthResponseStatus;
import com.ajaxjs.oauth.enums.AuthUserGender;
import com.ajaxjs.oauth.enums.scope.AuthAmazonScope;
import com.ajaxjs.oauth.model.AuthException;
import com.ajaxjs.oauth.utils.*;
import com.alibaba.fastjson.JSONObject;
import com.xkcoding.http.constants.Constants;
import com.xkcoding.http.support.HttpHeader;
import com.xkcoding.http.util.UrlUtil;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.oauth.model.AuthCallback;
import com.ajaxjs.oauth.model.AuthResponse;
import com.ajaxjs.oauth.model.AuthToken;
import com.ajaxjs.oauth.model.AuthUser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Amazon登录
 * Login with Amazon for Websites Overview： https://developer.amazon.com/zh/docs/login-with-amazon/register-web.html
 * Login with Amazon SDK for JavaScript Reference Guide：https://developer.amazon.com/zh/docs/login-with-amazon/javascript-sdk-reference.html
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @since 1.16.0
 */
public class AuthAmazonRequest extends AuthDefaultRequest {

    public AuthAmazonRequest(AuthConfig config) {
        super(config, AuthDefaultSource.AMAZON);
    }

    public AuthAmazonRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.AMAZON, authStateCache);
    }

    /**
     * https://developer.amazon.com/zh/docs/login-with-amazon/authorization-code-grant.html#authorization-request
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return String
     */
    @Override
    public String authorize(String state) {
        String realState = getRealState(state);
        UrlBuilder builder = UrlBuilder.fromBaseUrl(source.authorize())
            .queryParam("client_id", config.getClientId())
            .queryParam("scope", this.getScopes(" ", true, AuthChecker.getDefaultScopes(AuthAmazonScope.values())))
            .queryParam("redirect_uri", config.getRedirectUri())
            .queryParam("response_type", "code")
            .queryParam("state", realState);

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
     * https://developer.amazon.com/zh/docs/login-with-amazon/authorization-code-grant.html#access-token-request
     *
     * @return access token
     */
    @Override
    public AuthToken getAccessToken(AuthCallback authCallback) {
        Map<String, String> form = new HashMap<>(9);
        form.put("grant_type", "authorization_code");
        form.put("code", authCallback.getCode());
        form.put("redirect_uri", config.getRedirectUri());
        form.put("client_id", config.getClientId());
        form.put("client_secret", config.getClientSecret());

        if (config.isPkce()) {
            String cacheKey = this.source.getName().concat(":code_verifier:").concat(authCallback.getState());
            String codeVerifier = this.authStateCache.get(cacheKey);
            form.put("code_verifier", codeVerifier);
        }
        return getToken(form, this.source.accessToken());
    }

    @Override
    public AuthResponse<AuthToken> refresh(AuthToken authToken) {
        Map<String, String> form = new HashMap<>(7);
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", authToken.getRefreshToken());
        form.put("client_id", config.getClientId());
        form.put("client_secret", config.getClientSecret());
        return AuthResponse.<AuthToken>builder()
            .code(AuthResponseStatus.SUCCESS.getCode())
            .data(getToken(form, this.source.refresh()))
            .build();

    }

    private AuthToken getToken(Map<String, String> param, String url) {
        HttpHeader httpHeader = new HttpHeader();
        httpHeader.add("Host", "api.amazon.com");
        httpHeader.add(Constants.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");
        String response = new HttpUtils(config.getHttpConfig()).post(url, param, httpHeader, false).getBody();
        JSONObject jsonObject = JSONObject.parseObject(response);
        this.checkResponse(jsonObject);
        return AuthToken.builder()
            .accessToken(jsonObject.getString("access_token"))
            .tokenType(jsonObject.getString("token_type"))
            .expireIn(jsonObject.getIntValue("expires_in"))
            .refreshToken(jsonObject.getString("refresh_token"))
            .build();
    }

    /**
     * 校验响应内容是否正确
     *
     * @param jsonObject 响应内容
     */
    private void checkResponse(JSONObject jsonObject) {
        if (jsonObject.containsKey("error")) {
            throw new AuthException(jsonObject.getString("error_description").concat(" ") + jsonObject.getString("error_description"));
        }
    }

    /**
     * https://developer.amazon.com/zh/docs/login-with-amazon/obtain-customer-profile.html#call-profile-endpoint
     *
     * @param authToken token信息
     * @return AuthUser
     */
    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        String accessToken = authToken.getAccessToken();
        this.checkToken(accessToken);

        HttpHeader httpHeader = new HttpHeader();
        httpHeader.add("Host", "api.amazon.com");
        httpHeader.add("Authorization", "bearer " + accessToken);
        String userInfo = new HttpUtils(config.getHttpConfig()).get(this.source.userInfo(), new HashMap<>(0), httpHeader, false).getBody();
        JSONObject jsonObject = JSONObject.parseObject(userInfo);
        this.checkResponse(jsonObject);

        return AuthUser.builder()
            .rawUserInfo(jsonObject)
            .uuid(jsonObject.getString("user_id"))
            .username(jsonObject.getString("name"))
            .nickname(jsonObject.getString("name"))
            .email(jsonObject.getString("email"))
            .gender(AuthUserGender.UNKNOWN)
            .source(source.toString())
            .token(authToken)
            .build();
    }

    private void checkToken(String accessToken) {
        String tokenInfo = new HttpUtils(config.getHttpConfig()).get("https://api.amazon.com/auth/o2/tokeninfo?access_token=" + UrlUtil.urlEncode(accessToken)).getBody();
        JSONObject jsonObject = JSONObject.parseObject(tokenInfo);
        if (!config.getClientId().equals(jsonObject.getString("aud"))) {
            throw new AuthException(AuthResponseStatus.ILLEGAL_TOKEN);
        }
    }

    @Override
    protected String userInfoUrl(AuthToken authToken) {
        return UrlBuilder.fromBaseUrl(source.userInfo())
            .queryParam("user_id", authToken.getUserId())
            .queryParam("screen_name", authToken.getScreenName())
            .queryParam("include_entities", true)
            .build();
    }
}
