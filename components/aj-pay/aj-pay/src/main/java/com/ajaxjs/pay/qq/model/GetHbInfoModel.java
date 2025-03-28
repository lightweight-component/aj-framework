package com.ajaxjs.pay.qq.model;

import com.ajaxjs.pay.core.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 查询红包详情
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GetHbInfoModel extends BaseModel {
	private String send_type;
	private String nonce_str;
	private String mch_id;
	private String mch_billno;
	private String listid;
	private String sub_mch_id;
	private String sign;
}
