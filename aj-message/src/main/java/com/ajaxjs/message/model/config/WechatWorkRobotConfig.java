package com.ajaxjs.message.model.config;

import lombok.*;

/**
 * 企业微信-群机器人配置
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WechatWorkRobotConfig extends Config {
    @ConfigValue(value = "群机器人的webhook")
    private String webhook;
}
