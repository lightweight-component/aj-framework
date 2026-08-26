package com.ajaxjs.pay.paypal;

import lombok.Data;

@Data
public class ApiConfig {
    /**
     * 应用编号
     */
    private String clientId;

    /**
     * 应用密钥
     */
    private String secret;

    /**
     * 是否是沙箱环境
     */
    private boolean sandBox;

    /**
     * 域名
     */
    private String domain;
}
