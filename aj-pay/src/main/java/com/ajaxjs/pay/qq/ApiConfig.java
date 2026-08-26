package com.ajaxjs.pay.qq;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiConfig {
    private String appId;

    private String mchId;

    private String slAppId;

    private String slMchId;

    private String partnerKey;

    private String domain;

    private String certPath;

    private Object exParams;
}
