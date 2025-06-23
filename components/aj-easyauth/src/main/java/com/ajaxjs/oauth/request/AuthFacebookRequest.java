package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.model.enums.ResponseStatus;
import com.ajaxjs.oauth.model.enums.UserGender;
import com.ajaxjs.oauth.enums.scope.AuthFacebookScope;
import com.ajaxjs.oauth.model.AuthException;
import com.ajaxjs.oauth.utils.AuthChecker;
import com.ajaxjs.oauth.utils.GlobalAuthUtils;
import com.ajaxjs.oauth.utils.UrlBuilder;
import com.alibaba.fastjson.JSONObject;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.oauth.model.AuthCallback;
import com.ajaxjs.oauth.model.AuthToken;
import com.ajaxjs.oauth.model.AuthUser;

/**
 * Facebook登录
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @since 1.3.0
 */
public class AuthFacebookRequest extends AuthDefaultRequest {

    public AuthFacebookRequest(AuthConfig config) {
        super(config, AuthDefaultSource.FACEBOOK);
    }

    public AuthFacebookRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.FACEBOOK, authStateCache);
    }

    @Override
    public AuthToken getAccessToken(AuthCallback authCallback) {
        String response = doPostAuthorizationCode(authCallback.getCode());
        JSONObject accessTokenObject = JSONObject.parseObject(response);
        this.checkResponse(accessTokenObject);
        return AuthToken.builder()
            .accessToken(accessTokenObject.getString("access_token"))
            .expireIn(accessTokenObject.getIntValue("expires_in"))
            .tokenType(accessTokenObject.getString("token_type"))
            .build();
    }

    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        String userInfo = doGetUserInfo(authToken);
        JSONObject object = JSONObject.parseObject(userInfo);
        this.checkResponse(object);
        return AuthUser.builder()
            .rawUserInfo(object)
            .uuid(object.getString("id"))
            .username(object.getString("name"))
            .nickname(object.getString("name"))
            .blog(object.getString("link"))
            .avatar(getUserPicture(object))
            .location(object.getString("locale"))
            .email(object.getString("email"))
            .gender(UserGender.getRealGender(object.getString("gender")))
            .token(authToken)
            .source(source.toString())
            .build();
    }

    private String getUserPicture(JSONObject object) {
        String picture = null;
        if (object.containsKey("picture")) {
            JSONObject pictureObj = object.getJSONObject("picture");
            pictureObj = pictureObj.getJSONObject("data");
            if (null != pictureObj) {
                picture = pictureObj.getString("url");
            }
        }
        return picture;
    }

    /**
     * 返回获取userInfo的url
     *
     * @param authToken 用户token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AuthToken authToken) {
        return UrlBuilder.fromBaseUrl(source.userInfo())
            .queryParam("access_token", authToken.getAccessToken())
            .queryParam("fields", "id,name,birthday,gender,hometown,email,devices,picture.width(400),link")
            .build();
    }

    @Override
    protected void checkConfig(AuthConfig config) {
        super.checkConfig(config);
        // facebook的回调地址必须为https的链接
        if (AuthDefaultSource.FACEBOOK == source && !GlobalAuthUtils.isHttpsProtocol(config.getRedirectUri())) {
            // Facebook's redirect uri must use the HTTPS protocol
            throw new AuthException(ResponseStatus.ILLEGAL_REDIRECT_URI, source);
        }
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("error")) {
            throw new AuthException(object.getJSONObject("error").getString("message"));
        }
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(super.authorize(state))
            .queryParam("scope", this.getScopes(",", false, AuthChecker.getDefaultScopes(AuthFacebookScope.values())))
            .build();
    }
}
