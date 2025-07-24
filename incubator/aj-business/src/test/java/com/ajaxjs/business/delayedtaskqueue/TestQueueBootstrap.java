package com.ajaxjs.business.delayedtaskqueue;


import com.ajaxjs.business.delayedtaskqueue.bootstrap.QueueBootstrap;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 延时队列的时间轮实现
 * 多机的话，只要保证taskId全局唯一就行
 * <p>
 * https://blog.csdn.net/it_man/article/details/78402837
 * https://gitee.com/itman666/wheel-timer-queue
 */
@Slf4j
@SuppressWarnings("all")
public class TestQueueBootstrap {
    /**
     * 单线程测试
     */
    @Test
    public void testStart() {
        QueueBootstrap queueBootstrap = new QueueBootstrap();
        WheelQueue wheelQueue = queueBootstrap.start();

        wheelQueue.add(new AbstractTask("9527") {
            @Override
            public void run() {
                log.info("add task. id=" + this.getId());
            }

        }, 5);

        wheelQueue.add(new AbstractTask("9528") {
            @Override
            public void run() {
                log.info("running task. id=" + this.getId());
            }

        }, 8);

        wheelQueue.add(new AbstractTask("9529") {
            @Override
            public void run() {
                log.info("running task. id=" + this.getId());
            }

        }, 9);

        while (true) {
        }
    }

    /***
     * 多线程加入
     */
    @Test
    @SuppressWarnings("all")
    public void testThreadStart() {
        int threadCount = 3;
        final int sleep = 200;
        final int secondsRandom = 8000;
        QueueBootstrap queueBootstrap = new QueueBootstrap();
        final WheelQueue wheelQueue = queueBootstrap.start();
        Thread thread;

        for (int i = 1; i <= threadCount; i++) {
            thread = new Thread(new Runnable() {
                final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

                @Override
                public void run() {
                    while (true) {
                        wheelQueue.add(new AbstractTask(generateId()) {

                            @Override
                            public void run() {
                                log.debug("business processes. id=" + this.getId());
                            }

                        }, threadLocalRandom.nextInt(0, secondsRandom));

                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });

            thread.start();
        }

        while (true) {
        }
    }

    static AtomicInteger counter = new AtomicInteger(0);

    private static String generateId() {
        String s = String.format("C%05d", counter.incrementAndGet());
        return s;
    }

    @Test
    public void testShutdown() {
    }

}
