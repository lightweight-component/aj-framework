package com.ajaxjs.tracing.interceptor;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * 非 Servlet 上下文执行完成后移除拦截器，对 Servlet 上下文场景同样适用
 */
public interface TracingCustomizer extends MethodInterceptor {
}
