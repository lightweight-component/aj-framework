package com.ajaxjs.pay.qq.model;

import com.ajaxjs.pay.core.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>IJPay 让支付触手可及，封装了微信支付、支付宝支付、银联支付常用的支付方式以及各种常用的接口。</p>
 *
 * <p>不依赖任何第三方 mvc 框架，仅仅作为工具使用简单快速完成支付模块的开发，可轻松嵌入到任何系统里。 </p>
 *
 * <p>IJPay 交流群: 723992875、864988890</p>
 *
 * <p>Node.js 版: <a href="https://gitee.com/javen205/TNWX">https://gitee.com/javen205/TNWX</a></p>
 *
 * <p>创建现金红包 Model</p>
 *
 * @author Javen
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CreateHbModel extends BaseModel {
	private String charset;
	private String nonce_str;
	private String sign;
	private String mch_billno;
	private String mch_id;
	private String mch_name;
	private String qqappid;
	private String re_openid;
	private String total_amount;
	private String total_num;
	private String wishing;
	private String act_name;
	private String icon_id;
	private String banner_id;
	private String notify_url;
	private String not_send_msg;
	private String min_value;
	private String max_value;
}
