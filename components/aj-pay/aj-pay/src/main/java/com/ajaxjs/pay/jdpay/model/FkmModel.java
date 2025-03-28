package com.ajaxjs.pay.jdpay.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 付款码支付接口
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FkmModel extends JdBaseModel {
	private String token;
	private String version;
	private String merchant;
	private String device;
	private String tradeNum;
	private String tradeName;
	private String tradeDesc;
	private String tradeTime;
	private String amount;
	private String industryCategoryCode;
	private String currency;
	private String note;
	private String notifyUrl;
	private String orderGoodsNum;
	private String vendorId;
	private String goodsInfoList;
	private String receiverInfo;
	private String termInfo;
	private String payMerchant;
	private String sign;
	private String riskInfo;
	private String bizTp;
}
