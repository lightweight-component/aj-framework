package com.ajaxjs.business.logqueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SalesLogQueue {
    /**
     * 队列大小
     */
    public static final int QUEUE_MAX_SIZE = 1000;

    private static final SalesLogQueue alarmMessageQueue = new SalesLogQueue();

    //阻塞队列
    private final BlockingQueue<SalesLog> blockingQueue = new LinkedBlockingQueue<>(QUEUE_MAX_SIZE);

    private SalesLogQueue() {
    }

    public static SalesLogQueue getInstance() {
        return alarmMessageQueue;
    }

    /**
     * 消息入队
     */
    public boolean push(SalesLog salesLog) {
        return blockingQueue.add(salesLog);//队列满了就抛出异常，不阻塞
    }

    /**
     * 消息出队
     */
    public SalesLog poll() {
        try {
            return blockingQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取队列大小
     */
    public int size() {
        return blockingQueue.size();
    }
}