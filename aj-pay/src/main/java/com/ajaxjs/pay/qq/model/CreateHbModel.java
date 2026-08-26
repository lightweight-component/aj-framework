package com.ajaxjs.pay.qq.model;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 创建现金红包
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CreateHbModel {
    private String charset;
    private String nonce_str;
    private String sign;
    private String mch_billno;
    private String mch_id;
    private String mch_name;
    private String qqappid;
    private String re_openid;
    private String total_amount;
    private String total_num;
    private String wishing;
    private String act_name;
    private String icon_id;
    private String banner_id;
    private String notify_url;
    private String not_send_msg;
    private String min_value;
    private String max_value;
}
