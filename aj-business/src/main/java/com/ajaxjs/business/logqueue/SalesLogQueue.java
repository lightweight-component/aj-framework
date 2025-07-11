package com.ajaxjs.business.logqueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SalesLogQueue {
    //队列大小
    public static final int QUEUE_MAX_SIZE = 1000;

    private static SalesLogQueue alarmMessageQueue = new SalesLogQueue();

    //阻塞队列
    private BlockingQueue<SalesLogQueue> blockingQueue = new LinkedBlockingQueue<>(QUEUE_MAX_SIZE);

    private SalesLogQueue() {
    }

    public static SalesLogQueue getInstance() {
        return alarmMessageQueue;
    }

    /**
     * 消息入队
     *
     * @param salesLog
     * @return
     */
    public boolean push(SalesLogQueue salesLog) {
        return blockingQueue.add(salesLog);//队列满了就抛出异常，不阻塞
    }

    /**
     * 消息出队
     *
     * @return
     */
    public SalesLogQueue poll() {
        SalesLogQueue result = null;

        try {
            result = blockingQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 获取队列大小
     *
     * @return
     */
    public int size() {
        return blockingQueue.size();
    }
}