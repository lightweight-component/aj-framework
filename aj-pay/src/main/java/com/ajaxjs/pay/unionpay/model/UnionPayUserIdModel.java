package com.ajaxjs.pay.unionpay.model;

import lombok.*;
import lombok.experimental.Accessors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UnionPayUserIdModel {
    private String service;
    private String version;
    private String charset;
    private String sign_type;
    private String mch_id;
    private String nonce_str;
    private String sign;
    private String user_auth_code;
    private String app_up_identifier;
    private String sign_agentno;
    private String groupno;
}
