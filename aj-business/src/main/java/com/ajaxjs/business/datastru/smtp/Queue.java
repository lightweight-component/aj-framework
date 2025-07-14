package com.ajaxjs.business.datastru.smtp;

public class Queue {
    private final Request[] data;

    int size;

    private int head;

    private int tail;

    public Queue() {
        size = head = tail = 0;
        data = new Request[3000];
    }

    public synchronized Request deQueue() {
        while (size == 0) {
            try {
                wait();
            } catch (Exception ex) {
            }

        }

        Request tmp = data[head];
        data[head] = null;
        head = (head + 1) % data.length;
        size--;

        if (size == data.length - 1) notifyAll();

        return tmp;
    }

    public synchronized void enQueue(Request c) {
        while (size == data.length) {
            try {
                wait();
            } catch (Exception ex) {
            }

        }

        data[tail++] = c;
        tail %= data.length;
        size++;

        if (size == 1) notifyAll();
    }

    public boolean isFull() {
        return size == data.length;
    }




}


