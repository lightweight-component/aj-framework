package com.ajaxjs.pay.xpay.model;

import com.ajaxjs.pay.xpay.PayUrl;


public enum AliPayUrlEnum implements PayUrl {
	/**
	 * 支付宝付款码支付
	 */
	ALI_CODE_PAY("/api/pay/alipay/codePay"),

	/**
	 * 支付宝扫码支付
	 */
	ALI_NATIVE_PAY("/api/pay/alipay/nativePay"),

	/**
	 * 支付宝 H5 支付
	 */
	ALI_WAP_PAY("/api/pay/alipay/wapPay"),

	/**
	 * 支付宝 JS 支付
	 */
	ALI_JS_PAY("/api/pay/alipay/jsPay"),

	/**
	 * H5支付
	 */
	ALI_MOBILE_PAY("/api/pay/alipay/mobilePay"),

	/**
	 * 支付宝 APP 支付
	 */
	ALI_APP_PAY("/api/pay/wxpay/appPay"),

	/**
	 * 电脑网站支付
	 */
	ALI_WEB_PAY("/api/pay/alipay/webPay"),

	/**
	 * 关闭订单
	 */
	ALI_CLOSE_ORDER("/api/pay/alipay/closeOrder"),

	/**
	 * 撤销订单
	 */
	ALI_REVERSE_ORDER("/api/pay/alipay/reverseOrder"),

	/**
	 * 支付宝退款
	 */
	ALI_PAY_REFUND_ORDER("/api/pay/alipay/refundOrder"),

	/**
	 * 支付宝查询退款
	 */
	ALI_PAY_REFUND_QUERY("/api/pay/alipay/getRefundResult"),
	;

	/**
	 * 接口 url
	 */
	private final String url;

	AliPayUrlEnum(String url) {
		this.url = url;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return url;
	}
}
