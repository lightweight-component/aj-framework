package com.ajaxjs.business.delayedtaskqueue.bootstrap;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The default thread factory
 */
public class MyDefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    private final ThreadGroup group = Thread.currentThread().getThreadGroup();
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    MyDefaultThreadFactory(String groupName) {
        namePrefix = groupName + "-pool-" + POOL_NUMBER.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon())
            t.setDaemon(false);

        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);

        return t;
    }
}