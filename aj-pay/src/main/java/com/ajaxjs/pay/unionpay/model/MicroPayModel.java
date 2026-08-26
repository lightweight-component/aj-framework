package com.ajaxjs.pay.unionpay.model;

import lombok.*;
import lombok.experimental.Accessors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MicroPayModel {
    private String service;
    private String version;
    private String charset;
    private String sign_type;
    private String mch_id;
    private String out_trade_no;
    private String device_info;
    private String body;
    private String goods_detail;
    private String sub_appid;
    private String attach;
    private String need_receipt;
    private String total_fee;
    private String mch_create_ip;
    private String auth_code;
    private String time_start;
    private String time_expire;
    private String op_user_id;
    private String op_shop_id;
    private String op_device_id;
    private String goods_tag;
    private String nonce_str;
    private String sign;
    private String sign_agentno;
    private String groupno;
}
