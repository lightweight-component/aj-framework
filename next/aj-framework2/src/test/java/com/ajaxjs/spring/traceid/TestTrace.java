package com.ajaxjs.spring.traceid;

import com.ajaxjs.spring.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestTrace extends BaseTest {
    @Autowired
    AsyncService asyncService;
    @Test
    void testAsync() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Void> voidCompletableFuture = asyncService.asyncTask();
        // 等待 Future 完成，最多等待 10 秒
        voidCompletableFuture.get(10, TimeUnit.SECONDS);
    }
}
