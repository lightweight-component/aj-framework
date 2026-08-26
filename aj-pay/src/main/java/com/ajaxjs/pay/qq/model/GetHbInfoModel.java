package com.ajaxjs.pay.qq.model;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * 查询红包详情
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GetHbInfoModel {
    private String send_type;
    private String nonce_str;
    private String mch_id;
    private String mch_billno;
    private String listid;
    private String sub_mch_id;
    private String sign;
}
