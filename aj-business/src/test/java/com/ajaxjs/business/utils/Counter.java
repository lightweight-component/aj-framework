package com.ajaxjs.business.utils;

import org.junit.jupiter.api.Test;

/**
 * 该代码实现了一个线程安全的计数器，通过 synchronized 关键字确保多线程环境下 count 变量的递增操作和获取操作具有原子性和可见性。
 */
public class Counter {
    private int count = 0;

    // 使用 synchronized 修饰方法
    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }

    @Test
    public void test() {
        Counter counter = new Counter();

        // 创建多个线程共享同一个 Counter 实例
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++)
                counter.increment();
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++)
                counter.increment();
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Final count is: " + counter.getCount());
    }
}