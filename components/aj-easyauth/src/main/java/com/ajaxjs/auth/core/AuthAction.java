package com.ajaxjs.auth.core;

import com.ajaxjs.auth.model.Callback;
import com.ajaxjs.auth.model.Token;
import com.ajaxjs.oauth.model.enums.ResponseStatus;
import com.ajaxjs.oauth.model.*;

/**
 * The common business logics of authorization.
 */
public interface AuthAction {
    /**
     * Returns the authorization URL with the {@code state} parameter, which will be included in the callback after authorization.
     *
     * @param state A parameter used to validate the authorization flow; it helps prevent CSRF attacks
     * @return The authorization URL
     */
    default String authorize(String state) {
        throw new AuthException(ResponseStatus.NOT_IMPLEMENTED);
    }

    /**
     * To get the access token
     *
     * @param callback 授权成功后的回调参数
     * @return token
     */
    Token getAccessToken(Callback callback);

    /**
     * 使用 token 换取用户信息
     *
     * @param token token 信息
     * @return 用户信息
     */
    AuthUser getUserInfo(Token token);

    /**
     * 第三方登录
     *
     * @param callback 用于接收回调参数的实体
     * @return 返回登录成功后的用户信息
     */
    default AuthResponse<AuthUser> login(Callback callback) {
        throw new AuthException(ResponseStatus.NOT_IMPLEMENTED);
    }

    /**
     * 撤销授权
     *
     * @param token 登录成功后返回的Token信息
     * @return AuthResponse
     */
    default AuthResponse revoke(Token token) {
        throw new AuthException(ResponseStatus.NOT_IMPLEMENTED);
    }

    /**
     * 刷新access token （续期）
     *
     * @param token 登录成功后返回的Token信息
     * @return AuthResponse
     */
    default AuthResponse<AuthToken> refresh(Token token) {
        throw new AuthException(ResponseStatus.NOT_IMPLEMENTED);
    }
}
