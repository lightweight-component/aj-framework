package com.ajaxjs.pay.wxpay.enums.v2;

import com.ajaxjs.pay.wxpay.enums.WxApiEnum;

/**
 * <p>IJPay 让支付触手可及，封装了微信支付、支付宝支付、银联支付常用的支付方式以及各种常用的接口。</p>
 *
 * <p>不依赖任何第三方 mvc 框架，仅仅作为工具使用简单快速完成支付模块的开发，可轻松嵌入到任何系统里。 </p>
 *
 * <p>IJPay 交流群: 723992875、864988890</p>
 * <p>IJPay 自由交流群: 864988890</p>
 *
 * <p>Node.js 版: <a href="https://gitee.com/javen205/TNWX">https://gitee.com/javen205/TNWX</a></p>
 *
 * <p>微信支付 v2 版本-刷脸支付相关接口枚举</p>
 *
 * @author Javen
 */
public enum FacePayApiEnum implements WxApiEnum {

	/**
	 * 获取调用凭证
	 */
	GET_AUTH_INFO("/face/get_wxpayface_authinfo", "获取调用凭证"),

	/**
	 * 刷脸支付
	 */
	FACE_PAY("/pay/facepay", "刷脸支付"),

	/**
	 * 查询刷脸支付订单
	 */
	FACE_PAY_QUERY("/pay/facepayqueryy", "查询刷脸支付订单"),

	/**
	 * 撤销刷脸支付订单
	 */
	FACE_PAY_REVERSE("/secapi/pay/facepayreverse", "撤销刷脸支付订单"),
	;

	/**
	 * 接口URL
	 */
	private final String url;

	/**
	 * 接口描述
	 */
	private final String desc;

	FacePayApiEnum(String url, String desc) {
		this.url = url;
		this.desc = desc;
	}

	/**
	 * 获取枚举URL
	 *
	 * @return 枚举编码
	 */
	@Override
	public String getUrl() {
		return url;
	}

	/**
	 * 获取详细的描述信息
	 *
	 * @return 描述信息
	 */
	@Override
	public String getDesc() {
		return desc;
	}

	@Override
	public String toString() {
		return url;
	}
}
