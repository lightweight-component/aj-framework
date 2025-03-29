
package com.ajaxjs.pay.jdpay.model;

import com.ajaxjs.util.StrUtil;
import com.ajaxjs.pay.core.model.BaseModel;
import com.ajaxjs.pay.jdpay.JdPayKit;

import java.util.ArrayList;
import java.util.Map;

public class JdBaseModel extends BaseModel {
	/**
	 * 自动生成请求接口的 xml
	 *
	 * @param rsaPrivateKey RSA 私钥
	 * @param strDesKey     DES 密钥
	 * @param version       版本号
	 * @param merchant      商户号
	 * @return 生成的 xml 数据
	 */
	public String genReqXml(String rsaPrivateKey, String strDesKey, String version, String merchant) {
		if (StrUtil.isEmptyText(version) || StrUtil.isEmptyText(merchant))
			throw new RuntimeException("version or merchant is empty");

		String encrypt = JdPayKit.encrypt(rsaPrivateKey, strDesKey, JdPayKit.toJdXml(toMap()));
		Map<String, String> requestMap = JdRequestModel.builder()
			.version(version)
			.merchant(merchant)
			.encrypt(encrypt)
			.build()
			.toMap();

		return JdPayKit.toJdXml(requestMap);
	}

	/**
	 * PC H5 支付创建签名
	 *
	 * @param rsaPrivateKey RSA 私钥
	 * @param strDesKey     DES 密钥
	 * @return 生成签名后的 Map
	 */
	public Map<String, String> createSign(String rsaPrivateKey, String strDesKey) {
		Map<String, String> map = toMap();
		// 生成签名
		String sign = JdPayKit.signRemoveSelectedKeys(map, rsaPrivateKey, new ArrayList<>());
		map.put("sign", sign);
		// 3DES进行加密
		return JdPayKit.threeDesToMap(map, strDesKey);
	}
}
