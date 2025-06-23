
package com.ajaxjs.pay.unionpay;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.ajaxjs.util.StrUtil;

public class UnionPayApiConfigKit {
	private static final ThreadLocal<String> TL = new ThreadLocal<>();

	private static final Map<String, UnionPayApiConfig> CFG_MAP = new ConcurrentHashMap<>();

	private static final String DEFAULT_CFG_KEY = "_default_key_";

	/**
	 * 添加云闪付配置，每个 mchId 只需添加一次，相同 mchId 将被覆盖
	 *
	 * @param UnionPayApiConfig 云闪付配置
	 * @return {@link UnionPayApiConfig} 云闪付配置
	 */
	public static UnionPayApiConfig putApiConfig(UnionPayApiConfig UnionPayApiConfig) {
		if (CFG_MAP.isEmpty()) {
			CFG_MAP.put(DEFAULT_CFG_KEY, UnionPayApiConfig);
		}
		return CFG_MAP.put(UnionPayApiConfig.getMchId(), UnionPayApiConfig);
	}

	public static UnionPayApiConfig setThreadLocalApiConfig(UnionPayApiConfig UnionPayApiConfig) {
		if (StrUtil.hasText(UnionPayApiConfig.getMchId()))
			setThreadLocalMchId(UnionPayApiConfig.getMchId());

		return putApiConfig(UnionPayApiConfig);
	}

	public static UnionPayApiConfig removeApiConfig(UnionPayApiConfig UnionPayApiConfig) {
		return removeApiConfig(UnionPayApiConfig.getMchId());
	}

	public static UnionPayApiConfig removeApiConfig(String mchId) {
		return CFG_MAP.remove(mchId);
	}

	public static void setThreadLocalMchId(String mchId) {
		if (StrUtil.isEmptyText(mchId))
			mchId = CFG_MAP.get(DEFAULT_CFG_KEY).getMchId();

		TL.set(mchId);
	}

	public static void removeThreadLocalMchId() {
		TL.remove();
	}

	public static String getMchId() {
		String appId = TL.get();
		if (StrUtil.isEmptyText(appId))
			appId = CFG_MAP.get(DEFAULT_CFG_KEY).getMchId();

		return appId;
	}

	public static UnionPayApiConfig getApiConfig() {
		String appId = getMchId();
		return getApiConfig(appId);
	}

	public static UnionPayApiConfig getApiConfig(String appId) {
		UnionPayApiConfig cfg = CFG_MAP.get(appId);
		if (cfg == null)
			throw new IllegalStateException("需事先调用 UnionPayApiConfigKit.putApiConfig(UnionPayApiConfig) 将 mchId 对应的 UnionPayApiConfig 对象存入，才可以使用 UnionPayApiConfigKit.getUnionPayApiConfig() 的系列方法");

		return cfg;
	}
}
