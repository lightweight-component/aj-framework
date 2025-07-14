package com.ajaxjs.business.thread.cb;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierDemo {
    public static void main(String[] args) {
        final int count = 5;
        final CyclicBarrier barrier = new CyclicBarrier(count, () -> System.out.println("drink beer!"));

        // they do not have to start at the same time...
        for (int i = 0; i < count; i++)
            new Thread(new Worker(i, barrier)).start();
    }
}

class Worker implements Runnable {
    final int id;
    final CyclicBarrier barrier;

    public Worker(final int id, final CyclicBarrier barrier) {
        this.id = id;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {
            System.out.println(id + "starts to run !");
            Thread.sleep((long) (Math.random() * 10000));
            System.out.println(id + "arrived !");
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}