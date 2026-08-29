package com.ajaxjs.framework.validator;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Bean Validator 的可绑定配置属性。
 */
@ConfigurationProperties("ajaxjs.beanvalidator")
public class ValidatorProperties {
    /** 是否启用自动配置。 */
    private boolean enabled = true;
    /** 注解消息占位符与实际提示语的映射。 */
    private Map<String, String> messages = new HashMap<>();

    /**
     * 创建并填充默认校验提示语。
     */
    public ValidatorProperties() {
        messages.put("id-card", "身份证号格式不正确");
        messages.put("mobile-no", "手机号格式不正确");
        messages.put("username", "用户名格式不正确");
        messages.put("password", "密码强度不足");
        messages.put("chinese", "必须为中文");
        messages.put("ipv4", "IPv4 地址格式不正确");
        messages.put("http-url", "HTTP 地址格式不正确");
        messages.put("not-null", "不能为 null");
        messages.put("not-blank", "不能为空白");
        messages.put("size", "长度不符合要求");
        messages.put("min", "值不能小于最小值");
        messages.put("max", "值不能大于最大值");
        messages.put("pattern", "格式不正确");
        messages.put("email", "Email 格式不正确");
    }

    /**
     * 获取消息映射。
     *
     * @return 消息映射
     */
    public Map<String, String> getMessages() {
        return messages;
    }

    /**
     * 判断是否启用校验组件。
     *
     * @return 启用时为 {@code true}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用校验组件。
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 设置消息映射。
     *
     * @param messages 消息映射
     */
    public void setMessages(Map<String, String> messages) {
        this.messages = messages;
    }
}
