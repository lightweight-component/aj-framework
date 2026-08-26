package com.ajaxjs.pay.unionpay.model;

import lombok.*;
import lombok.experimental.Accessors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BillDownloadModel {
    private String service;
    private String version;
    private String charset;
    private String bill_date;
    private String bill_type;
    private String sign_type;
    private String mch_id;
    private String nonce_str;
    private String sign;
}
