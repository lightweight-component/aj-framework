package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthExtendSource;
import com.ajaxjs.oauth.enums.AuthResponseStatus;
import com.ajaxjs.oauth.enums.AuthUserGender;
import com.ajaxjs.oauth.model.AuthCallback;
import com.ajaxjs.oauth.model.AuthResponse;
import com.ajaxjs.oauth.model.AuthToken;
import com.ajaxjs.oauth.model.AuthUser;

/**
 * 测试用自定义扩展的第三方request
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @version 1.0
 * @since 1.12.0
 */
public class AuthExtendRequest extends AuthDefaultRequest {

    public AuthExtendRequest(AuthConfig config) {
        super(config, AuthExtendSource.OTHER);
    }

    public AuthExtendRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthExtendSource.OTHER, authStateCache);
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
        return AuthToken.builder()
            .openId("openId")
            .expireIn(1000)
            .idToken("idToken")
            .scope("scope")
            .refreshToken("refreshToken")
            .accessToken("accessToken")
            .code("code")
            .build();
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
        return AuthUser.builder()
            .username("test")
            .nickname("test")
            .gender(AuthUserGender.MALE)
            .token(authToken)
            .source(this.source.toString())
            .build();
    }

    /**
     * 撤销授权
     *
     * @param authToken 登录成功后返回的Token信息
     * @return AuthResponse
     */
    @Override
    public AuthResponse revoke(AuthToken authToken) {
        return AuthResponse.builder()
            .code(AuthResponseStatus.SUCCESS.getCode())
            .msg(AuthResponseStatus.SUCCESS.getMsg())
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
        return AuthResponse.<AuthToken>builder()
            .code(AuthResponseStatus.SUCCESS.getCode())
            .data(AuthToken.builder()
                .openId("openId")
                .expireIn(1000)
                .idToken("idToken")
                .scope("scope")
                .refreshToken("refreshToken")
                .accessToken("accessToken")
                .code("code")
                .build())
            .build();
    }
}
