package com.ajaxjs.message.model.config;

import lombok.*;

/**
 * 企业微信配置
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WechatWorkAgentConfig extends Config {
    @ConfigValue(value = "企业ID", description = "在此页面查看：https://work.weixin.qq.com/wework_admin/frame#profile")
    private String corpId;

    @ConfigValue(value = "应用Secret")
    private String secret;

    @ConfigValue(value = "应用agentId")
    private Integer agentId;
}
