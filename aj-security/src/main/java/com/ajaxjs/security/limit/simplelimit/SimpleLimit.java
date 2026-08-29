package com.ajaxjs.security.limit.simplelimit;

import com.ajaxjs.security.InterceptorAction;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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

    /**
     * Stores the semaphore value.
     */
    private static Semaphore semaphore;

    /**
     * Executes the simple limit operation.
     */
    public SimpleLimit() {
        if (semaphore == null)
            semaphore = new Semaphore(maxSemaphore);
    }

    /**
     * Executes the action operation.
     *
     * @param annotation the annotation parameter.
     * @param req        the req parameter.
     * @return the operation result.
     */
    @Override
    public boolean action(SimpleLimitCheck annotation, HttpServletRequest req) {
        boolean acquired = false;

        try {
            acquired = semaphore.tryAcquire(1, TimeUnit.MILLISECONDS);
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

    /**
     * Executes the get after completion action operation.
     *
     * @return the operation result.
     */
    @Override
    public BiConsumer<HttpServletRequest, HttpServletResponse> getAfterCompletionAction() {
        return (req, resp) -> {
            System.out.println("afterCompletion:" + count++);
//            semaphore.release(); // 释放许可
        };
    }
}
