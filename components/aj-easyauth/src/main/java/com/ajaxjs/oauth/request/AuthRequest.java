package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.enums.AuthResponseStatus;
import com.ajaxjs.oauth.model.AuthException;
import com.ajaxjs.oauth.model.AuthCallback;
import com.ajaxjs.oauth.model.AuthResponse;
import com.ajaxjs.oauth.model.AuthToken;
import com.ajaxjs.oauth.model.AuthUser;

/**
 * JustAuth {@code Request}公共接口，所有平台的{@code Request}都需要实现该接口
 * <p>
 * {@link AuthRequest#authorize()}
 * {@link AuthRequest#authorize(String)}
 * {@link AuthRequest#login(AuthCallback)}
 * {@link AuthRequest#revoke(AuthToken)}
 * {@link AuthRequest#refresh(AuthToken)}
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @since 1.8
 */
public interface AuthRequest {

    /**
     * 返回授权url，可自行跳转页面
     * <p>
     * 不建议使用该方式获取授权地址，不带{@code state}的授权地址，容易受到csrf攻击。
     * 建议使用{@link AuthDefaultRequest#authorize(String)}方法生成授权地址，在回调方法中对{@code state}进行校验
     *
     * @return 返回授权地址
     */
    @Deprecated
    default String authorize() {
        throw new AuthException(AuthResponseStatus.NOT_IMPLEMENTED);
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    default String authorize(String state) {
        throw new AuthException(AuthResponseStatus.NOT_IMPLEMENTED);
    }

    /**
     * 获取access token
     *
     * @param authCallback 授权成功后的回调参数
     * @return token
     * @see AuthDefaultRequest#authorize()
     * @see AuthDefaultRequest#authorize(String)
     */
    AuthToken getAccessToken(AuthCallback authCallback);

    /**
     * 使用token换取用户信息
     *
     * @param authToken token信息
     * @return 用户信息
     * @see AuthDefaultRequest#getAccessToken(AuthCallback)
     */
     AuthUser getUserInfo(AuthToken authToken);

    /**
     * 第三方登录
     *
     * @param authCallback 用于接收回调参数的实体
     * @return 返回登录成功后的用户信息
     */
    default AuthResponse<AuthUser> login(AuthCallback authCallback) {
        throw new AuthException(AuthResponseStatus.NOT_IMPLEMENTED);
    }

    /**
     * 撤销授权
     *
     * @param authToken 登录成功后返回的Token信息
     * @return AuthResponse
     */
    default AuthResponse revoke(AuthToken authToken) {
        throw new AuthException(AuthResponseStatus.NOT_IMPLEMENTED);
    }

    /**
     * 刷新access token （续期）
     *
     * @param authToken 登录成功后返回的Token信息
     * @return AuthResponse
     */
    default AuthResponse<AuthToken> refresh(AuthToken authToken) {
        throw new AuthException(AuthResponseStatus.NOT_IMPLEMENTED);
    }
}
