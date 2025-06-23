package com.ajaxjs.pay.paypal.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AccessTokenCache 默认缓存实现，默认存储与内存中
 */
public class DefaultAccessTokenCache implements IAccessTokenCache {
	private final Map<String, String> map = new ConcurrentHashMap<>();

	@Override
	public String get(String key) {
		return map.get(key);
	}

	@Override
	public void set(String key, String jsonValue) {
		map.put(key, jsonValue);
	}

	@Override
	public void remove(String key) {
		map.remove(key);
	}

}
