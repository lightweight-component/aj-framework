package com.ajaxjs.pay.core.enums;


/**
 * 微信支付 v3 接口授权认证类型枚举
 */
public enum AuthTypeEnum {
	/**
	 * 国密
	 */
	SM2("WECHATPAY2-SM2-WITH-SM3", "AEAD_SM4_GCM", "国密算法"),
	/**
	 * RSA
	 */
	RSA("WECHATPAY2-SHA256-RSA2048", "AEAD_AES_256_GCM", "RSA算法"),
	;

	private final String code;

	private final String platformCertAlgorithm;

	private final String desc;

	AuthTypeEnum(String code, String platformCertAlgorithm, String desc) {
		this.code = code;
		this.platformCertAlgorithm = platformCertAlgorithm;
		this.desc = desc;
	}

	/**
	 * 获取枚举编码
	 *
	 * @return 枚举编码
	 */
	public String getCode() {
		return code;
	}

	/**
	 * 获取详细的描述信息
	 *
	 * @return 描述信息
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * 获取平台证书算法
	 *
	 * @return 平台证书算法
	 */
	public String getPlatformCertAlgorithm() {
		return platformCertAlgorithm;
	}
}
