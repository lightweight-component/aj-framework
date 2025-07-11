package com.ajaxjs.spring.mvc.unifiedreturn;

import lombok.Data;

@Data
public class ResponseResultWrapper {
    private Integer status;

    private String errorCode;

    private String message;

    private Object data;
}
