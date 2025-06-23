package com.ajaxjs.pay.xpay.model;

import com.ajaxjs.pay.core.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PayModel extends BaseModel {
	private String out_trade_no;
	private String total_fee;
	private String mch_id;
	private String body;
	private String type;
	private String openId;
	private String face_code;
	private String title;
	private String auth_code;
	private String attach;
	private String receipt;
	private String notify_url;
	private String return_url;
	private String config_no;
	private String auto;
	private String auto_node;
	private String sign;
	private String money;
	private String refund_no;
	private String refund_desc;
	private String status;
	private String order_no;
	private String pay_no;
	private String start_time;
	private String end_time;
	private String date;
	private String app_id;
	private String url;
	private String params;
	private String code;
	private String callback_url;
}
