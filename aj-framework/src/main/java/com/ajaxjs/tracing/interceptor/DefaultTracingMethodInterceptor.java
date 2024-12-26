package com.ajaxjs.tracing.interceptor;

import com.ajaxjs.tracing.LocalContextHolder;
import com.alibaba.ttl.TtlCallable;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 非 Servlet 上下文执行完成后移除拦截器，对 Servlet 上下文场景同样适用
 */
public class DefaultTracingMethodInterceptor implements TracingCustomizer {
    public DefaultTracingMethodInterceptor(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    private final ThreadPoolTaskExecutor taskExecutor;

    @Override
    public Object invoke(MethodInvocation invocation) {
        Future<Object> future = taskExecutor.submit(TtlCallable.get(() -> {
            try {
                return invocation.proceed();
            } catch (Throwable e) {
                throw new RuntimeException("DefaultTracingMethodInterceptor", e);
            } finally {
                LocalContextHolder.unbind(true);
            }
        }));

        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException("DefaultTracingMethodInterceptor InterruptedException", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("DefaultTracingMethodInterceptor ExecutionException", e);
        }
    }
}
