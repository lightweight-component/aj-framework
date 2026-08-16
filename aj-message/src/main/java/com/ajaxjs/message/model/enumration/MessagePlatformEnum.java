package com.ajaxjs.message.model.enumration;

import com.ajaxjs.message.model.config.*;
import com.ajaxjs.util.ObjectHelper;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息平台枚举
 */
@Getter
public enum MessagePlatformEnum {
    EMAIL(EmailConfig.class, "邮箱", "", "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,})$", true),
    WECHAT_WORK_AGENT(WechatWorkAgentConfig.class, "企业微信-应用消息", "", "", true),
    WECHAT_WORK_ROBOT(WechatWorkRobotConfig.class, "企业微信-群机器人", "", "", true),
    WECHAT_OFFICIAL_ACCOUNT(WechatOfficialAccountConfig.class, "微信公众号", "", "", true),
    DING_TALK_CORP(DingTalkCorpConfig.class, "钉钉-工作通知", "", "", true),
    DING_TALK_ROBOT(DingTalkRobotConfig.class, "钉钉-群机器人", "", "", true),
    RPUSH_SERVER(EmptyConfig.class, "rpush服务", "", "", true);

    private final String name;

    private final String description;

    /**
     * 校验用的正则表达式
     */
    private final String validateReg;

    private final boolean enable;

    /**
     * 配置类型
     */
    private final Class<? extends Config> configType;

    MessagePlatformEnum(Class<? extends Config> configType, String name, String description, String validateReg, boolean enable) {
        this.name = name;
        this.description = description;
        this.validateReg = validateReg;
        this.enable = enable;
        this.configType = configType;
    }

    public boolean isEnable() {
        return enable;
    }

    public Class<? extends Config> getConfigType() {
        return configType;
    }

    public boolean matcher(String testStr) {
        if (ObjectHelper.isEmptyText(testStr) || ObjectHelper.isEmptyText(validateReg))
            return true;

        Pattern pattern = Pattern.compile(validateReg);
        Matcher matcher = pattern.matcher(testStr);
        return matcher.matches();
    }

    public void matcherThrow(String testStr) {
        Assert.isTrue(matcher(testStr), testStr + "格式不符，请检查后重试");
    }
}
