/**
 * <p>IJPay 让支付触手可及，封装了微信支付、支付宝支付、银联支付常用的支付方式以及各种常用的接口。</p>
 *
 * <p>不依赖任何第三方 mvc 框架，仅仅作为工具使用简单快速完成支付模块的开发，可轻松嵌入到任何系统里。 </p>
 *
 * <p>IJPay 交流群: 723992875、864988890</p>
 *
 * <p>Node.js 版: <a href="https://gitee.com/javen205/TNWX">https://gitee.com/javen205/TNWX</a></p>
 *
 * <p>云闪付-统一下单</p>
 *
 * @author Javen
 */
package com.ajaxjs.pay.unionpay.model;

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
public class UnifiedOrderModel extends BaseModel {
	private String service;
	private String version;
	private String charset;
	private String sign_type;
	private String mch_id;
	private String appid;
	private String is_raw;
	private String is_minipg;
	private String out_trade_no;
	private String device_info;
	private String op_shop_id;
	private String body;
	private String sub_openid;
	private String user_id;
	private String attach;
	private String sub_appid;
	private String total_fee;
	private String need_receipt;
	private String customer_ip;
	private String mch_create_ip;
	private String notify_url;
	private String time_start;
	private String time_expire;
	private String qr_code_timeout_express;
	private String op_user_id;
	private String goods_tag;
	private String product_id;
	private String nonce_str;
	private String buyer_logon_id;
	private String buyer_id;
	private String limit_credit_pay;
	private String sign;
	private String sign_agentno;
	private String groupno;
}
