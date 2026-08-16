package com.ajaxjs.message.model.config;

import lombok.*;

/**
 * 钉钉群机器人消息配置
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DingTalkRobotConfig extends Config {
    private static final long serialVersionUID = -9206902816158196669L;

    @ConfigValue(value = "群机器人的webhook")
    private String webhook;
}
