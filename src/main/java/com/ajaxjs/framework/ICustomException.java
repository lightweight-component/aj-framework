package com.ajaxjs.framework;

/**
 * 自定义的业务异常
 */
public interface ICustomException {
    /**
     * 返回自定义的 HTTP 状态码
     */
    int getErrCode();
}