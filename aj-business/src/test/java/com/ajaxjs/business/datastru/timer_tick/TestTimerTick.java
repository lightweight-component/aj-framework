package com.ajaxjs.business.datastru.timer_tick;



import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestTimerTick {
    @Test
    public void test() {
        Random random = new Random();
        System.out.println(LocalDateTime.now() + " - Program started.");
        TimerTick timerTick = TimerTick.init(key -> System.out.println("Do something for " + key));
        ExecutorService exec = Executors.newCachedThreadPool();

        for (int i = 100; i < 110; i++) {
            final int key = i;

            exec.execute(() -> {
                try {
                    Thread.sleep(random.nextInt(20000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println(LocalDateTime.now() + " - add KEY: " + key);
                timerTick.add(key);
            });
        }

        exec.shutdown();
    }
}
