package com.ajaxjs.business.datastru.timer_tick;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 简易倒计时工具类
 */
public class TimerTick {
    private final static ConcurrentLinkedQueue<Ele> queue = new ConcurrentLinkedQueue<>();
    private Consumer<Long> callback = null;
    public final int period = 1000 * 5; //10s
    public final long expireTime = 1000 * 12; //1min

    public static TimerTick init(Consumer<Long> callback) {
        TimerTick timerTick = new TimerTick();
        timerTick.start(callback);

        return timerTick;
    }

    public void add(long key) {
        queue.offer(new Ele(key));
    }

    private void start(Consumer<Long> callback) {
        this.callback = callback;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                doJob();
            }
        }, 1000, period);
    }

    private void doJob() {
        // 获取队列元素
        Ele e = queue.peek();

        // 空队列什么都不执行
        if (e == null) {
            System.out.println(LocalDateTime.now() + " - Queue is empty.");

            return;
        }

        // 判断是否超时
        if (isExpired(e.getCtime(), expireTime)) {
            System.out.println(LocalDateTime.now() + " - KEY:" + e.getKey() + " is expired.");
            queue.poll(); //从队列中移出第一个元素
            callback.accept(e.getKey()); //执行回调函数

            doJob();
        } else {
//            System.out.println(LocalDateTime.now() + " - Do nothing.");
        }
    }

    private boolean isExpired(long cTime, long expireTime) {
//        System.out.println("ctime: " + ctime + ", expirTime: " + expirTime + ", now: " + System.currentTimeMillis());
        return cTime + expireTime < System.currentTimeMillis();
    }

}