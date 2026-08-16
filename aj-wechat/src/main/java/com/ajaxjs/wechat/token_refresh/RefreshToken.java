package com.ajaxjs.wechat.token_refresh;

import java.util.function.Supplier;

/**
 * The implementation for this function interface should refresh token, finally return a new token
 */
@FunctionalInterface
public interface RefreshToken extends Supplier<TokenWithExpire> {
}
