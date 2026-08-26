package com.ajaxjs.pay.unionpay.model;

import lombok.*;
import lombok.experimental.Accessors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RefundModel {
    private String service;
    private String version;
    private String charset;
    private String sign_type;
    private String mch_id;
    private String out_trade_no;
    private String transaction_id;
    private String out_refund_no;
    private String total_fee;
    private String refund_fee;
    private String op_user_id;
    private String refund_channel;
    private String nonce_str;
    private String sign;
    private String sign_agentno;
    private String groupno;
}
