package com.ajaxjs.pay.qq.model;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 企业付款对账单下载
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DownloadTransferBillModel {
    private String mch_id;
    private String nonce_str;
    private String bill_date;
    private String sign;
}
