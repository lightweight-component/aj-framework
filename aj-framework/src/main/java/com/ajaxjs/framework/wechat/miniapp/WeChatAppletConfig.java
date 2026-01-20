package com.ajaxjs.framework.wechat.miniapp;

import lombok.Data;

/**
 * 微信小程序服务端配置
 */
@Data
public class WeChatAppletConfig {
    /**
     * App ID
     */
    private String accessKeyId;

    /**
     * App 密钥
     */
    private String accessSecret;

    /**
     * 访问令牌
     */
    private String accessToken;
}
