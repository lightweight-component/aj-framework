package com.ajaxjs.framework.validator;

/**
 * 自定义校验注解的声明或配置不正确。
 */
public class ValidatorConfigurationException extends ValidatorException {
    /**
     * 使用配置错误说明创建异常。
     *
     * @param errorMsg 配置错误说明
     */
    public ValidatorConfigurationException(String errorMsg) {
        super(errorMsg);
    }
}
