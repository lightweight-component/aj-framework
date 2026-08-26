package com.ajaxjs.pay.qq.model;

import lombok.*;
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
public class DownloadBillModel {
    private String appid;
    private String mch_id;
    private String nonce_str;
    private String sign;
    private String bill_date;
    private String bill_type;
    private String tar_type;
}
