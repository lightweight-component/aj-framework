package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.oauth.enums.AuthResponseStatus;
import com.ajaxjs.oauth.enums.AuthUserGender;
import com.ajaxjs.oauth.enums.scope.AuthQqScope;
import com.ajaxjs.oauth.model.*;
import com.ajaxjs.oauth.utils.*;
import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * qq登录
 */
public class AuthQqRequest extends AuthDefaultRequest {
    public AuthQqRequest(AuthConfig config) {
        super(config, AuthDefaultSource.QQ);
    }

    public AuthQqRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.QQ, authStateCache);
    }

    @Override
    public AuthToken getAccessToken(AuthCallback authCallback) {
        String response = doGetAuthorizationCode(authCallback.getCode());
        return getAuthToken(response);
    }

    @Override
    public AuthResponse<AuthToken> refresh(AuthToken authToken) {
        String response = new HttpUtils(config.getHttpConfig()).get(refreshTokenUrl(authToken.getRefreshToken())).getBody();
        return AuthResponse.<AuthToken>builder().code(AuthResponseStatus.SUCCESS.getCode()).data(getAuthToken(response)).build();
    }

    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        String openId = this.getOpenId(authToken);
        String response = doGetUserInfo(authToken);
        JSONObject object = JSONObject.parseObject(response);
        if (object.getIntValue("ret") != 0) {
            throw new AuthException(object.getString("msg"));
        }
        String avatar = object.getString("figureurl_qq_2");
        if (StrUtil.isEmptyTextText(avatar)) {
            avatar = object.getString("figureurl_qq_1");
        }

        String location = String.format("%s-%s", object.getString("province"), object.getString("city"));
        return AuthUser.builder()
            .rawUserInfo(object)
            .username(object.getString("nickname"))
            .nickname(object.getString("nickname"))
            .avatar(avatar)
            .location(location)
            .uuid(openId)
            .gender(AuthUserGender.getRealGender(object.getString("gender")))
            .token(authToken)
            .source(source.toString())
            .build();
    }

    /**
     * 获取QQ用户的OpenId，支持自定义是否启用查询unionid的功能，如果启用查询unionid的功能，
     * 那就需要开发者先通过邮件申请unionid功能，参考链接 {@see http://wiki.connect.qq.com/unionid%E4%BB%8B%E7%BB%8D}
     *
     * @param authToken 通过{@link AuthQqRequest#getAccessToken(AuthCallback)}获取到的{@code authToken}
     * @return openId
     */
    private String getOpenId(AuthToken authToken) {
        String response = new HttpUtils(config.getHttpConfig()).get(UrlBuilder.fromBaseUrl("https://graph.qq.com/oauth2.0/me")
            .queryParam("access_token", authToken.getAccessToken())
            .queryParam("unionid", config.isUnionId() ? 1 : 0)
            .build()).getBody();
        String removePrefix = response.replace("callback(", "");
        String removeSuffix = removePrefix.replace(");", "");
        String openId = removeSuffix.trim();
        JSONObject object = JSONObject.parseObject(openId);
        if (object.containsKey("error")) {
            throw new AuthException(object.get("error") + ":" + object.get("error_description"));
        }
        authToken.setOpenId(object.getString("openid"));
        if (object.containsKey("unionid")) {
            authToken.setUnionId(object.getString("unionid"));
        }
        return StrUtil.isEmptyTextText(authToken.getUnionId()) ? authToken.getOpenId() : authToken.getUnionId();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param authToken 用户授权token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AuthToken authToken) {
        return UrlBuilder.fromBaseUrl(source.userInfo())
            .queryParam("access_token", authToken.getAccessToken())
            .queryParam("oauth_consumer_key", config.getClientId())
            .queryParam("openid", authToken.getOpenId())
            .build();
    }

    private AuthToken getAuthToken(String response) {
        Map<String, String> accessTokenObject = EncodeTools.parseStringToMap(response);
        if (!accessTokenObject.containsKey("access_token") || accessTokenObject.containsKey("code"))
            throw new AuthException(accessTokenObject.get("msg"));

        return AuthToken.builder()
            .accessToken(accessTokenObject.get("access_token"))
            .expireIn(Integer.parseInt(accessTokenObject.getOrDefault("expires_in", "0")))
            .refreshToken(accessTokenObject.get("refresh_token"))
            .build();
    }

    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(super.authorize(state))
            .queryParam("scope", this.getScopes(",", false, AuthChecker.getDefaultScopes(AuthQqScope.values())))
            .build();
    }
}
