package com.ajaxjs.pay.wxpay;

import com.ajaxjs.util.StrUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WxPayApiConfigKit {
	private static final ThreadLocal<String> TL = new ThreadLocal<>();

	private static final Map<String, WxPayApiConfig> CFG_MAP = new ConcurrentHashMap<>();
	private static final String DEFAULT_CFG_KEY = "_default_key_";

	/**
	 * 添加微信支付配置，每个appId只需添加一次，相同appId将被覆盖
	 *
	 * @param wxPayApiConfig 微信支付配置
	 * @return {WxPayApiConfig} 微信支付配置
	 */
	public static WxPayApiConfig putApiConfig(WxPayApiConfig wxPayApiConfig) {
		return putApiConfig(wxPayApiConfig.getAppId(), wxPayApiConfig);
	}

	public static WxPayApiConfig putApiConfig(String key, WxPayApiConfig wxPayApiConfig) {
		if (CFG_MAP.isEmpty()) {
			CFG_MAP.put(DEFAULT_CFG_KEY, wxPayApiConfig);
		}
		return CFG_MAP.put(key, wxPayApiConfig);
	}

	public static WxPayApiConfig setThreadLocalWxPayApiConfig(WxPayApiConfig wxPayApiConfig) {
		return setThreadLocalWxPayApiConfig(wxPayApiConfig.getAppId(), wxPayApiConfig);
	}

	public static WxPayApiConfig setThreadLocalWxPayApiConfig(String key, WxPayApiConfig wxPayApiConfig) {
		if (StrUtil.isEmptyTextText(key))
			key = wxPayApiConfig.getAppId();

		setThreadLocalAppId(key);
		return putApiConfig(key, wxPayApiConfig);
	}

	public static WxPayApiConfig removeApiConfig(WxPayApiConfig wxPayApiConfig) {
		return removeApiConfig(wxPayApiConfig.getAppId());
	}

	public static WxPayApiConfig removeApiConfig(String appId) {
		return CFG_MAP.remove(appId);
	}

	public static void setThreadLocalAppId(String appId) {
		if (StrUtil.isEmptyTextText(appId))
			appId = CFG_MAP.get(DEFAULT_CFG_KEY).getAppId();

		TL.set(appId);
	}

	public static void removeThreadLocalAppId() {
		TL.remove();
	}

	public static String getAppId() {
		String appId = TL.get();
		if (StrUtil.isEmptyTextText(appId))
			appId = CFG_MAP.get(DEFAULT_CFG_KEY).getAppId();

		return appId;
	}

	public static WxPayApiConfig getWxPayApiConfig() {
		String appId = getAppId();
		return getApiConfig(appId);
	}

	public static WxPayApiConfig getApiConfig(String appId) {
		WxPayApiConfig cfg = CFG_MAP.get(appId);
		if (cfg == null) {
			throw new IllegalStateException("需事先调用 WxPayApiConfigKit.putApiConfig(wxPayApiConfig) 将 appId 对应的 WxPayApiConfig 对象存入，才可以使用 WxPayApiConfigKit.getWxPayApiConfig() 的系列方法");
		}
		return cfg;
	}
}
