package com.ajaxjs.spring.traceid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AsyncService {
    @Async("MyExecutor")
    public CompletableFuture<Void> asyncTask() {
        log.info("Async task started");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 重要：恢复中断状态
            log.error("Async task was interrupted", e);
            return CompletableFuture.completedFuture(null); // 或者处理中断
        }

        log.info("Async task completed");
        return CompletableFuture.completedFuture(null);
    }
}
