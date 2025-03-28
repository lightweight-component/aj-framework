package com.ajaxjs.pay.wxpay.model;

import com.ajaxjs.pay.core.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 授权码查询
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AuthCodeToOpenIdModel extends BaseModel {
	private String appid;
	private String sub_appid;
	private String mch_id;
	private String sub_mch_id;
	private String auth_code;
	private String nonce_str;
	private String sign;
	private String sign_type;

}
