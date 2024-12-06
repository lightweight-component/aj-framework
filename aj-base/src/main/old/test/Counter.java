package com.ajaxjs.base;

import org.junit.jupiter.api.Test;

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