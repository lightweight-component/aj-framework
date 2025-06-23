package com.ajaxjs.auth.model;

import com.ajaxjs.oauth.config.AuthSource;
import com.ajaxjs.auth.model.enums.ResponseStatus;

/**
 * Auth Exception
 */
public class AuthException extends RuntimeException {
    private int errorCode;

    private String errorMsg;

    public AuthException(String errorMsg) {
        this(ResponseStatus.FAILURE.getCode(), errorMsg);
    }

    public AuthException(String errorMsg, AuthSource source) {
        this(ResponseStatus.FAILURE.getCode(), errorMsg, source);
    }

    public AuthException(int errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public AuthException(ResponseStatus status) {
        this(status.getCode(), status.getMsg());
    }

    public AuthException(int errorCode, String errorMsg, AuthSource source) {
        this(errorCode, String.format("%s [%s]", errorMsg, source.getName()));
    }

    public AuthException(ResponseStatus status, AuthSource source) {
        this(status.getCode(), status.getMsg(), source);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException(Throwable cause) {
        super(cause);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
