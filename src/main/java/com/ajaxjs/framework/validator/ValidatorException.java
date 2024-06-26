package com.ajaxjs.framework.validator;

/**
 * @author xuhaohao
 */
public class ValidatorException extends RuntimeException {
    public ValidatorException(Throwable cause) {
        super(cause);
    }

    public ValidatorException(String errorMsg) {
        super(errorMsg);
    }
}
