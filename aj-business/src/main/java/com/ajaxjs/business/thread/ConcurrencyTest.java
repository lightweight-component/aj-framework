package com.ajaxjs.business.thread;


import java.util.concurrent.*;


/**
 * 模拟并发
 * <a href="https://blog.csdn.net/u012811805/article/details/122227835">...</a>
 */
public class ConcurrencyTest {

    // 请求总数
    public static int clientTotal = 5000;

    // 同时并发执行的线程总数
    public static int threadTotal = 200;

    public static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        // 定义线程池
        ExecutorService executorService = Executors.newCachedThreadPool();
        // 定义信号量 最大的线程数量
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);

        for (int i = 0; i < clientTotal; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    add();
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();
        executorService.shutdown();
        System.out.println(count); // 因为count不是线程安全的，且没有作防护措施，结果是错的
    }

    private static void add() {
        count++;
    }
}