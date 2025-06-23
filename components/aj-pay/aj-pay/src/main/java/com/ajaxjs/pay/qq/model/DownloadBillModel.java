package com.ajaxjs.pay.qq.model;

import com.ajaxjs.pay.core.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 对账单下载
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DownloadBillModel extends BaseModel {
	private String appid;
	private String mch_id;
	private String nonce_str;
	private String sign;
	private String bill_date;
	private String bill_type;
	private String tar_type;
}
