package com.ajaxjs.business.datastru.smtp;

/**
 * 队列类，用于存储和管理邮件发送请求
 * 该类实现了典型的环形缓冲区逻辑，以高效地处理请求的入队和出队操作
 */
public class Queue {
    // 队列数据数组，存储邮件发送请求
    private final Request[] data;

    // 队列中当前元素的数量
    int size;

    // 队列头部索引，用于出队操作
    private int head;

    // 队列尾部索引，用于入队操作
    private int tail;

    /**
     * 默认构造函数，初始化队列
     * 设置初始的头尾指针位置，以及创建一个固定大小的Request数组
     */
    public Queue() {
        size = head = tail = 0;
        data = new Request[3000];
    }

    /**
     * 出队操作，从队列头部移除并返回一个邮件发送请求
     * 如果队列为空，则线程等待直到队列中有元素
     *
     * @return 移除并返回的邮件发送请求
     */
    public synchronized Request deQueue() {
        // 当队列为空时，等待生产者线程添加元素
        while (size == 0) {
            try {
                wait();
            } catch (Exception ex) {
                // 异常处理为空，通常应记录日志或进行其他处理
            }
        }

        Request tmp = data[head];
        data[head] = null;
        head = (head + 1) % data.length;
        size--;

        // 当队列即将满时，通知可能等待入队的线程
        if (size == data.length - 1)
            notifyAll();

        return tmp;
    }

    /**
     * 入队操作，将一个邮件发送请求添加到队列尾部
     * 如果队列已满，则线程等待直到队列中有空闲位置
     *
     * @param c 要添加到队列的邮件发送请求
     */
    public synchronized void enQueue(Request c) {
        // 当队列已满时，等待消费者线程移除元素
        while (size == data.length) {
            try {
                wait();
            } catch (Exception ex) {
                // 异常处理为空，通常应记录日志或进行其他处理
            }
        }

        data[tail++] = c;
        tail %= data.length;
        size++;

        // 当队列从空变非空时，通知可能等待出队的线程
        if (size == 1)
            notifyAll();
    }

    /**
     * 检查队列是否已满
     *
     * @return 如果队列已满返回true，否则返回false
     */
    public boolean isFull() {
        return size == data.length;
    }
}
