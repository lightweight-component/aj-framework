package com.ajaxjs.framework.validator;

/**
 * 校验失败时抛出的运行时异常。
 */
public class ValidatorException extends RuntimeException {
    /**
     * 使用原始异常创建校验异常。
     *
     * @param cause 原始异常
     */
    public ValidatorException(Throwable cause) {
        super(cause);
    }

    /**
     * 使用校验失败说明创建异常。
     *
     * @param errorMsg 校验失败说明
     */
    public ValidatorException(String errorMsg) {
        super(errorMsg);
    }
}
