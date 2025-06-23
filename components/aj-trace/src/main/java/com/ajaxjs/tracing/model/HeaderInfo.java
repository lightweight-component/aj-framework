package com.ajaxjs.tracing.model;

/**
 * 用户信息常量
 */
public interface HeaderInfo {
    /**
     * 当一个请求通过一个或多个代理服务器传递时，这些代理的 IP 地址可以存储在这个请求头中
     */
    String X_FORWARDED_FOR = "x-forwarded-for";
    
    /**
     * 在一些特定的环境或应用中，这个请求头可能被用来表示客户端的真实IP地址
     */
    String PROXY_CLIENT_IP = "Proxy-Client-IP";
    
    /**
     * 在代理服务器或负载均衡器等网络设备中，该请求头用于传递客户端的真实 IP 地址，以便后端服务器或应用程序能够获取到正确的来源 IP
     */
    String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    
    /**
     * HTTP_CLIENT_IP 是一个在 PHP 中常用的变量，用于获取客户端的 IP 地址。这个变量通常被用来检查和记录来自客户端的请求的 IP 地址
     */
    String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    
    /**
     * 客户端的 IP 地址，通常是在 HTTP 请求头中传递的
     */
    String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
    
    /**
     * 事务唯一编号
     */
    String TRACE_ID = "traceId";
    
    /**
     * 多语言头
     */
    String LANGUAGE = "language";
    
    /**
     * 包类型
     */
    String APP_TYPE = "appType";
    
    /**
     * 包版本
     */
    String APP_VERSION = "appVersion";
    
    /**
     * 内容类型
     */
    String CONTENT_TYPE = "Content-Type";
}
