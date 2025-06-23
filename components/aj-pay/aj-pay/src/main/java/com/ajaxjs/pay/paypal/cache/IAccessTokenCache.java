package com.ajaxjs.pay.paypal.cache;

/**
 * AccessToken 缓存接口
 */
public interface IAccessTokenCache {
	String get(String key);

	void set(String key, String jsonValue);

	void remove(String key);
}
