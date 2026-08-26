package com.ajaxjs.pay.qq.model;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 现金红包对账单下载
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DownloadHbBillModel {
    private String sign;
    private String mch_id;
    private String date;
}
