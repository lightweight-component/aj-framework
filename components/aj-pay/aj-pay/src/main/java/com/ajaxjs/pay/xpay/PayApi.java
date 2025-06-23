package com.ajaxjs.pay.xpay;

import com.ajaxjs.pay.core.model.IJPayHttpResponse;
import com.ajaxjs.pay.core.kit.HttpKit;

import java.util.HashMap;
import java.util.Map;

public class PayApi {
	/**
	 * 获取接口请求的 URL
	 *
	 * @param payUrl    {@link PayUrl} 支付 API 接口枚举
	 * @param serverUrl 网关
	 * @return {@link String} 返回完整的接口请求URL
	 */
	public static String getReqUrl(String serverUrl, PayUrl payUrl) {
		return serverUrl.concat(payUrl.getUrl());
	}

	/**
	 * post 请求
	 *
	 * @param url     请求 url
	 * @param params  {@link Map} 请求参数
	 * @param headers {@link Map} 请求头
	 * @return {@link IJPayHttpResponse} 请求返回的结果
	 */
	public static IJPayHttpResponse post(String url, Map<String, Object> params, Map<String, String> headers) {
		return HttpKit.getDelegate().post(url, params, headers);
	}

	/**
	 * get 请求
	 *
	 * @param url     请求 url
	 * @param params  {@link Map} 请求参数
	 * @param headers {@link Map} 请求头
	 * @return {@link IJPayHttpResponse} 请求返回的结果
	 */
	public static IJPayHttpResponse get(String url, Map<String, Object> params, Map<String, String> headers) {
		return HttpKit.getDelegate().get(url, params, headers);
	}

	/**
	 * 执行请求
	 *
	 * @param serverUrl XPay 网关
	 * @param payUrl    {@link PayUrl} 支付 API 接口枚举
	 * @param params    请求参数
	 * @return {@link IJPayHttpResponse} 请求返回的结果
	 */
	public static IJPayHttpResponse exePost(String serverUrl, PayUrl payUrl, Map<String, String> params) {
		Map<String, Object> dataMap = new HashMap<String, Object>(params);
		return post(getReqUrl(serverUrl, payUrl), dataMap, null);
	}

	/**
	 * 执行请求
	 *
	 * @param serverUrl XPay 网关
	 * @param payUrl    {@link PayUrl} 支付 API 接口枚举
	 * @param params    请求参数
	 * @return {@link IJPayHttpResponse} 请求返回的结果
	 */
	public static IJPayHttpResponse exeGet(String serverUrl, PayUrl payUrl, Map<String, String> params) {
		Map<String, Object> dataMap = new HashMap<String, Object>(params);
		return get(getReqUrl(serverUrl, payUrl), dataMap, null);
	}
}
