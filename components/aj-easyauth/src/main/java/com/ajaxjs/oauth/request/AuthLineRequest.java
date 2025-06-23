package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.model.enums.ResponseStatus;
import com.ajaxjs.oauth.model.enums.UserGender;
import com.ajaxjs.oauth.enums.scope.AuthLineScope;
import com.ajaxjs.oauth.utils.AuthChecker;
import com.ajaxjs.oauth.utils.HttpUtils;
import com.ajaxjs.oauth.utils.UrlBuilder;
import com.alibaba.fastjson.JSONObject;
import com.xkcoding.http.support.HttpHeader;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.oauth.model.AuthCallback;
import com.ajaxjs.oauth.model.AuthResponse;
import com.ajaxjs.oauth.model.AuthToken;
import com.ajaxjs.oauth.model.AuthUser;

import java.util.HashMap;
import java.util.Map;

/**
 * LINE 登录, line.biz
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @since 1.16.0
 */
public class AuthLineRequest extends AuthDefaultRequest {

    public AuthLineRequest(AuthConfig config) {
        super(config, AuthDefaultSource.LINE);
    }

    public AuthLineRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.LINE, authStateCache);
    }

    @Override
    public AuthToken getAccessToken(AuthCallback authCallback) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", authCallback.getCode());
        params.put("redirect_uri", config.getRedirectUri());
        params.put("client_id", config.getClientId());
        params.put("client_secret", config.getClientSecret());
        String response = new HttpUtils(config.getHttpConfig()).post(source.accessToken(), params, false).getBody();
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        return AuthToken.builder()
            .accessToken(accessTokenObject.getString("access_token"))
            .refreshToken(accessTokenObject.getString("refresh_token"))
            .expireIn(accessTokenObject.getIntValue("expires_in"))
            .idToken(accessTokenObject.getString("id_token"))
            .scope(accessTokenObject.getString("scope"))
            .tokenType(accessTokenObject.getString("token_type"))
            .build();
    }

    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        String userInfo = new HttpUtils(config.getHttpConfig()).get(source.userInfo(), null, new HttpHeader()
            .add("Content-Type", HttpConstants.CONTENT_TYPE_FORM
)
            .add("Authorization", "Bearer ".concat(authToken.getAccessToken())), false).getBody();
        JSONObject object = JSONObject.parseObject(userInfo);
        return AuthUser.builder()
            .rawUserInfo(object)
            .uuid(object.getString("userId"))
            .username(object.getString("displayName"))
            .nickname(object.getString("displayName"))
            .avatar(object.getString("pictureUrl"))
            .remark(object.getString("statusMessage"))
            .gender(UserGender.UNKNOWN)
            .token(authToken)
            .source(source.toString())
            .build();
    }

    @Override
    public AuthResponse revoke(AuthToken authToken) {
        Map<String, String> params = new HashMap<>(5);
        params.put("access_token", authToken.getAccessToken());
        params.put("client_id", config.getClientId());
        params.put("client_secret", config.getClientSecret());
        String userInfo = new HttpUtils(config.getHttpConfig()).post(source.revoke(), params, false).getBody();
        JSONObject object = JSONObject.parseObject(userInfo);
        // 返回1表示取消授权成功，否则失败
        ResponseStatus status = object.getBooleanValue("revoked") ? ResponseStatus.SUCCESS : ResponseStatus.FAILURE;
        return AuthResponse.builder().code(status.getCode()).msg(status.getMsg()).build();
    }

    @Override
    public AuthResponse<AuthToken> refresh(AuthToken oldToken) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", oldToken.getRefreshToken());
        params.put("client_id", config.getClientId());
        params.put("client_secret", config.getClientSecret());
        String response = new HttpUtils(config.getHttpConfig()).post(source.accessToken(), params, false).getBody();
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        return AuthResponse.<AuthToken>builder()
            .code(ResponseStatus.SUCCESS.getCode())
            .data(AuthToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .refreshToken(accessTokenObject.getString("refresh_token"))
                .expireIn(accessTokenObject.getIntValue("expires_in"))
                .idToken(accessTokenObject.getString("id_token"))
                .scope(accessTokenObject.getString("scope"))
                .tokenType(accessTokenObject.getString("token_type"))
                .build())
            .build();
    }

    @Override
    public String userInfoUrl(AuthToken authToken) {
        return UrlBuilder.fromBaseUrl(source.userInfo())
            .queryParam("user", authToken.getUid())
            .build();
    }

    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(super.authorize(state))
            .queryParam("nonce", state)
            .queryParam("scope", this.getScopes(" ", true, AuthChecker.getDefaultScopes(AuthLineScope.values())))
            .build();
    }
}
