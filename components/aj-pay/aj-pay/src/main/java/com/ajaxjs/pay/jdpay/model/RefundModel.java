package com.ajaxjs.pay.jdpay.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 退款申请接口
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RefundModel extends JdBaseModel {
	private String version;
	private String merchant;
	private String tradeNum;
	private String oTradeNum;
	private String amount;
	private String currency;
	private String tradeTime;
	private String notifyUrl;
	private String note;
	private String sign;
	private String device;
	private String termInfoId;
	private String cert;
}
