package com.ajaxjs.pay.jdpay.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 商户二维码支付接口
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CustomerPayModel extends JdBaseModel {
	private String version;
	private String sign;
	private String merchant;
	private String payMerchant;
	private String device;
	private String tradeNum;
	private String tradeName;
	private String tradeDesc;
	private String tradeTime;
	private String amount;
	private String orderType;
	private String industryCategoryCode;
	private String currency;
	private String note;
	private String callbackUrl;
	private String notifyUrl;
	private String ip;
	private String expireTime;
	private String riskInfo;
	private String goodsInfo;
	private String bizTp;
}
