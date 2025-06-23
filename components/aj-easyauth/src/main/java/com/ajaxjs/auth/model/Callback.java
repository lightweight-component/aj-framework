package com.ajaxjs.auth.model;

import lombok.Data;

@Data
public class Callback {
    /**
     * 访问 AuthorizeUrl 后回调时带的参数 code
     */
    private String code;

    /**
     * 访问 AuthorizeUrl后回调时带的参数 state，用于和请求 AuthorizeUrl 前的 state 比较，防止 CSRF 攻击
     */
    private String state;
}
