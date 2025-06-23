package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.model.enums.ResponseStatus;
import com.ajaxjs.oauth.enums.scope.AuthFigmaScope;
import com.ajaxjs.oauth.model.AuthException;
import com.ajaxjs.oauth.utils.*;
import com.alibaba.fastjson.JSONObject;
import com.xkcoding.http.support.HttpHeader;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.oauth.model.AuthCallback;
import com.ajaxjs.oauth.model.AuthResponse;
import com.ajaxjs.oauth.model.AuthToken;
import com.ajaxjs.oauth.model.AuthUser;

/**
 * Figma登录
 * @author xiangqian
 * @since 1.16.6
 */
public class AuthFigmaRequest extends AuthDefaultRequest {
    public AuthFigmaRequest(AuthConfig config) {
        super(config, AuthDefaultSource.FIGMA);
    }

    public AuthFigmaRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.FIGMA, authStateCache);
    }

    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(super.authorize(state))
            .queryParam("scope", this.getScopes(",", true, AuthChecker.getDefaultScopes(AuthFigmaScope.values())))
            .build();
    }

    @Override
    public AuthToken getAccessToken(AuthCallback authCallback) {
        HttpHeader header = new HttpHeader()
            .add("content-type", HttpConstants.CONTENT_TYPE_FORM
)
            .add("Authorization", "Basic " + Base64Utils.encode(config.getClientId().concat(":").concat(config.getClientSecret())));

        String response = new HttpUtils(config.getHttpConfig()).post(super.accessTokenUrl(authCallback.getCode()), null, header, true).getBody();
        JSONObject accessTokenObject = JSONObject.parseObject(response);

        this.checkResponse(accessTokenObject);

        return AuthToken.builder()
            .accessToken(accessTokenObject.getString("access_token"))
            .refreshToken(accessTokenObject.getString("refresh_token"))
            .scope(accessTokenObject.getString("scope"))
            .userId(accessTokenObject.getString("user_id"))
            .expireIn(accessTokenObject.getIntValue("expires_in"))
            .build();
    }

    @Override
    public AuthResponse<AuthToken> refresh(AuthToken authToken) {
        HttpHeader header = new HttpHeader().add("content-type", HttpConstants.CONTENT_TYPE_FORM
);
        String response = new HttpUtils(config.getHttpConfig()).post(this.refreshTokenUrl(authToken.getRefreshToken()), null, header, false).getBody();
        JSONObject dataObj = JSONObject.parseObject(response);

        this.checkResponse(dataObj);

        return AuthResponse.<AuthToken>builder()
            .code(ResponseStatus.SUCCESS.getCode())
            .data(AuthToken.builder()
                .accessToken(dataObj.getString("access_token"))
                .openId(dataObj.getString("open_id"))
                .expireIn(dataObj.getIntValue("expires_in"))
                .refreshToken(dataObj.getString("refresh_token"))
                .scope(dataObj.getString("scope"))
                .build())
            .build();

    }

    @Override
    protected String refreshTokenUrl(String refreshToken) {
        return UrlBuilder.fromBaseUrl(source.refresh())
            .queryParam("client_id", config.getClientId())
            .queryParam("client_secret", config.getClientSecret())
            .queryParam("refresh_token", refreshToken)
            .build();
    }

    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        HttpHeader header = new HttpHeader().add("Authorization", "Bearer " + authToken.getAccessToken());
        String response = new HttpUtils(config.getHttpConfig()).get(super.userInfoUrl(authToken), null, header, false).getBody();
        JSONObject dataObj = JSONObject.parseObject(response);

        this.checkResponse(dataObj);

        return AuthUser.builder()
            .rawUserInfo(dataObj)
            .uuid(dataObj.getString("id"))
            .username(dataObj.getString("handle"))
            .avatar(dataObj.getString("img_url"))
            .email(dataObj.getString("email"))
            .token(authToken)
            .source(source.toString())
            .build();
    }


    /**
     * 校验响应结果
     *
     * @param object 接口返回的结果
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new AuthException(object.getString("error") + ":" + object.getString("message"));
        }
    }
}
