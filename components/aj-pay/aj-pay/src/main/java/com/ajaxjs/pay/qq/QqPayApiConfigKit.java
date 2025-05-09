package com.ajaxjs.pay.qq;

import com.ajaxjs.util.StrUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>IJPay 让支付触手可及，封装了微信支付、支付宝支付、银联支付等常用的支付方式以及各种常用的接口。</p>
 *
 * <p>不依赖任何第三方 mvc 框架，仅仅作为工具使用简单快速完成支付模块的开发，可轻松嵌入到任何系统里。 </p>
 *
 * <p>IJPay 交流群: 723992875、864988890</p>
 *
 * <p>Node.js 版: <a href="https://gitee.com/javen205/TNWX">https://gitee.com/javen205/TNWX</a></p>
 *
 * <p>QQ 钱包支付常用配置 Kit</p>
 *
 * @author Javen
 */
public class QqPayApiConfigKit {
	private static final ThreadLocal<String> TL = new ThreadLocal<>();

	private static final Map<String, QqPayApiConfig> CFG_MAP = new ConcurrentHashMap<>();
	private static final String DEFAULT_CFG_KEY = "_default_key_";

	/**
	 * 添加微信支付配置，每个appId只需添加一次，相同appId将被覆盖
	 *
	 * @param qqPayApiConfig 微信支付配置
	 * @return {WxPayApiConfig} 微信支付配置
	 */
	public static QqPayApiConfig putApiConfig(QqPayApiConfig qqPayApiConfig) {
		if (CFG_MAP.isEmpty())
			CFG_MAP.put(DEFAULT_CFG_KEY, qqPayApiConfig);

		return CFG_MAP.put(qqPayApiConfig.getAppId(), qqPayApiConfig);
	}

	public static QqPayApiConfig setThreadLocalQqPayApiConfig(QqPayApiConfig qqPayApiConfig) {
		if (StrUtil.hasText(qqPayApiConfig.getAppId()))
			setThreadLocalAppId(qqPayApiConfig.getAppId());

		return putApiConfig(qqPayApiConfig);
	}

	public static QqPayApiConfig removeApiConfig(QqPayApiConfig qqPayApiConfig) {
		return removeApiConfig(qqPayApiConfig.getAppId());
	}

	public static QqPayApiConfig removeApiConfig(String appId) {
		return CFG_MAP.remove(appId);
	}

	public static void setThreadLocalAppId(String appId) {
		if (StrUtil.isEmptyText(appId)) {
			appId = CFG_MAP.get(DEFAULT_CFG_KEY).getAppId();
		}
		TL.set(appId);
	}

	public static void removeThreadLocalAppId() {
		TL.remove();
	}

	public static String getAppId() {
		String appId = TL.get();
		if (StrUtil.isEmptyText(appId)) {
			appId = CFG_MAP.get(DEFAULT_CFG_KEY).getAppId();
		}
		return appId;
	}

	public static QqPayApiConfig getQqPayApiConfig() {
		String appId = getAppId();
		return getApiConfig(appId);
	}

	public static QqPayApiConfig getApiConfig(String appId) {
		QqPayApiConfig cfg = CFG_MAP.get(appId);
		if (cfg == null)
			throw new IllegalStateException("需事先调用 QqPayApiConfigKit.putApiConfig(qqPayApiConfig) 将 appId 对应的 QqPayApiConfig 对象存入，才可以使用 QqPayApiConfigKit.getQqPayApiConfig() 的系列方法");

		return cfg;
	}
}
