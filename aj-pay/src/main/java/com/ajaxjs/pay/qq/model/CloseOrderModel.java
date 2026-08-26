package com.ajaxjs.pay.qq.model;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 关闭订单
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CloseOrderModel {
    private String appid;
    private String sub_appid;
    private String mch_id;
    private String sub_mch_id;
    private String nonce_str;
    private String sign;
    private String out_trade_no;
    private String total_fee;
}
