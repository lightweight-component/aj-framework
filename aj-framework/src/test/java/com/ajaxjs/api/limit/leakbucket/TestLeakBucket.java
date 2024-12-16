package com.ajaxjs.api.limit.leakbucket;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TestLeakBucket {
    /**
     * 线程池，用于多线程模拟测试
     */
    private ExecutorService pool = Executors.newFixedThreadPool(10);

    @Test
    void test() {
        // 被限制的次数
        AtomicInteger limited = new AtomicInteger(0);
        // 线程数
        int threads = 2;
        // 每条线程的执行轮数
        int turns = 20;

        // 同步器
        CountDownLatch countDownLatch = new CountDownLatch(threads);
        long start = System.currentTimeMillis();

        LeakyBucket leakyBucket = new LeakyBucket(2);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    for (int j = 0; j < turns; j++) {

                        long taskId = Thread.currentThread().getId();
                        boolean intercepted = leakyBucket.acquire();
                        if (intercepted)
                            // 被限制的次数累积
                            limited.getAndIncrement();


                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //等待所有线程结束
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        float time = (System.currentTimeMillis() - start) / 1000F;
        //输出统计结果

        log.info("限制的次数为：" + limited.get() + ",通过的次数为：" + (threads * turns - limited.get()));
        log.info("限制的比例为：" + (float) limited.get() / (float) (threads * turns));
        log.info("运行的时长为：" + time);
    }
}
