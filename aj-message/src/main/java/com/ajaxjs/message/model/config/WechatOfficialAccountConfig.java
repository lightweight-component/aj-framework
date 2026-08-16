package com.ajaxjs.message.model.config;

import lombok.*;

/**
 * 微信公众号配置
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WechatOfficialAccountConfig extends Config {
    private String appId;

    private String secret;
}
