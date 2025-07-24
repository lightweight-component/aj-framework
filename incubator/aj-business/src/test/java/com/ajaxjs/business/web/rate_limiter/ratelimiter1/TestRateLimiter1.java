package com.ajaxjs.business.web.rate_limiter.ratelimiter1;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestRateLimiter1 {
    @Test
    public void testRateCounter() {
        RateLimiter rateLimiter = new RateCounter(10);
        int num = 100;

        while (num > 0) {
            try {
                rateLimiter.acquire();
            } catch (Exception e) {
                continue;
            }

            num--;
            System.out.println("sec: " + System.currentTimeMillis() / 1000L + ", mil: " + System.currentTimeMillis() + " got a token");
        }
    }

    @Test
    public void testLeakyBucketLimiter() throws InterruptedException {
        RateLimiter rateLimiter = new LeakyBucketLimiter(1);

        Runnable runnable = () -> {
            int num = 100;

            while (num > 0) {
                try {
                    rateLimiter.acquire();
                } catch (Exception e) {
                    continue;
                }

                num--;
                System.out.println("Thread: " + Thread.currentThread().getName() + ", sec: " + System.currentTimeMillis() / 1000L + ", mil: " + System.currentTimeMillis() + " got a token");
            }
        };

        long start = System.currentTimeMillis();
        ExecutorService threadPool = Executors.newCachedThreadPool();

        for (int i = 0; i < 10; i++)
            threadPool.submit(runnable);

        threadPool.awaitTermination(100, TimeUnit.SECONDS);
        long end = System.currentTimeMillis();
        System.out.println("over time: " + (end - start) / 1000);
    }

    @Test
    public void testTokenBucketLimiter() throws InterruptedException {
        RateLimiter rateLimiter = new TokenBucketLimiter(10);

        Runnable runnable = () -> {
            int num = 100;

            while (num > 0) {
                try {
                    rateLimiter.acquire();
                } catch (Exception e) {
                    continue;
                }

                num--;
                System.out.println("Thread: " + Thread.currentThread().getName() + ", sec: " + System.currentTimeMillis() / 1000L + ", mil: " + System.currentTimeMillis() + " got a token");
            }
        };

        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++)
            threadPool.submit(runnable);

        threadPool.awaitTermination(100, TimeUnit.SECONDS);
    }
}
