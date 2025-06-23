package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.oauth.model.enums.UserGender;
import com.ajaxjs.oauth.enums.scope.AuthGithubScope;
import com.ajaxjs.oauth.model.AuthException;
import com.ajaxjs.oauth.utils.*;
import com.ajaxjs.util.EncodeTools;
import com.alibaba.fastjson.JSONObject;
import com.xkcoding.http.support.HttpHeader;
import com.ajaxjs.oauth.model.AuthCallback;
import com.ajaxjs.oauth.model.AuthToken;
import com.ajaxjs.oauth.model.AuthUser;

import java.util.Map;

/**
 * Github登录
 */
public class AuthGithubRequest extends AuthDefaultRequest {

    public AuthGithubRequest(AuthConfig config) {
        super(config, AuthDefaultSource.GITHUB);
    }

    public AuthGithubRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.GITHUB, authStateCache);
    }

    @Override
    public AuthToken getAccessToken(AuthCallback authCallback) {
        String response = doPostAuthorizationCode(authCallback.getCode());
        Map<String, String> res = EncodeTools.parseStringToMap(response);

        this.checkResponse(res.containsKey("error"), res.get("error_description"));

        return AuthToken.builder()
            .accessToken(res.get("access_token"))
            .scope(res.get("scope"))
            .tokenType(res.get("token_type"))
            .build();
    }

    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        HttpHeader header = new HttpHeader();
        header.add("Authorization", "token " + authToken.getAccessToken());
        String response = new HttpUtils(config.getHttpConfig()).get(UrlBuilder.fromBaseUrl(source.userInfo()).build(), null, header, false).getBody();
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object.containsKey("error"), object.getString("error_description"));

        return AuthUser.builder()
            .rawUserInfo(object)
            .uuid(object.getString("id"))
            .username(object.getString("login"))
            .avatar(object.getString("avatar_url"))
            .blog(object.getString("blog"))
            .nickname(object.getString("name"))
            .company(object.getString("company"))
            .location(object.getString("location"))
            .email(object.getString("email"))
            .remark(object.getString("bio"))
            .gender(UserGender.UNKNOWN)
            .token(authToken)
            .source(source.toString())
            .build();
    }

    private void checkResponse(boolean error, String errorDescription) {
        if (error) {
            throw new AuthException(errorDescription);
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
            .queryParam("scope", this.getScopes(" ", true, AuthChecker.getDefaultScopes(AuthGithubScope.values())))
            .build();
    }

}
