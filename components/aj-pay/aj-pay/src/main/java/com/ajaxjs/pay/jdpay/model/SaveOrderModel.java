package com.ajaxjs.pay.jdpay.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 在线支付接口
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SaveOrderModel extends JdBaseModel {
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
	private String specCardNo;
	private String specId;
	private String specName;
	private String userId;
	private String expireTime;
	private String orderGoodsNum;
	private String vendorId;
	private String goodsInfo;
	private String receiverInfo;
	private String termInfo;
	private String riskInfo;
	private String settleCurrency;
	private String kjInfo;
	private String installmentNum;
	private String preProduct;
	private String bizTp;
}
