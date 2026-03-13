package com.ajaxjs.security.limit.simplelimit;

import com.ajaxjs.security.InterceptorAction;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * 用 Semaphore 对接口进行限流
 */
@Data
@Component
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "security.limit.simple-limit.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "security.limit.simple-limit")
public class SimpleLimit extends InterceptorAction<SimpleLimitCheck> {
    /**
     * 最大信号量
     */
    int maxSemaphore = 3;

    private static Semaphore semaphore;

    public SimpleLimit() {
        if (semaphore == null)
            semaphore = new Semaphore(maxSemaphore);
    }

    @Override
    public boolean action(SimpleLimitCheck annotation, HttpServletRequest req) {
        boolean acquired = false;

        try {
            acquired = semaphore.tryAcquire(1,  TimeUnit.MILLISECONDS );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (!acquired) {// HTTP 429 Too Many Requests
//            semaphore.release();
            throw new SecurityException("Too many requests, please try again later.");
        }

        return true;
    }

    int count = 0;

    @Override
    public BiConsumer<HttpServletRequest, HttpServletResponse> getAfterCompletionAction() {
        return (req, resp) -> {
            System.out.println("afterCompletion:" + count++);
//            semaphore.release(); // 释放许可
        };
    }
}
