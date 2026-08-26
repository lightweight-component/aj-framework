package com.ajaxjs.pay.unionpay.model;

import lombok.*;
import lombok.experimental.Accessors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AuthCodeToOpenIdModel {
    private String service;
    private String version;
    private String charset;
    private String sign_type;
    private String mch_id;
    private String sub_appid;
    private String auth_code;
    private String nonce_str;
    private String sign;
    private String sign_agentno;
    private String groupno;
}
