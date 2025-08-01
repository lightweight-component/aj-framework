package com.ajaxjs.framework.mvc.unifiedreturn;

import lombok.Data;

@Data
public class ResponseResultWrapper {
    private Integer status;

    private String errorCode;

    private String traceId;

    private String message;

    private Object data;
}
