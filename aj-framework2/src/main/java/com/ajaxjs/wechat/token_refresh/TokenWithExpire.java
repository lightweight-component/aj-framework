package com.ajaxjs.wechat.token_refresh;

/**
 * Token with expiry time
 */
public interface TokenWithExpire {
    /**
     * Get token
     *
     * @return the token string
     */
    String getToken();

    /**
     * Get expire time, unit: second
     * 返回的是距今将来要过期的时间，单位是秒
     *
     * @return expire time
     */
    int getExpire();
}
