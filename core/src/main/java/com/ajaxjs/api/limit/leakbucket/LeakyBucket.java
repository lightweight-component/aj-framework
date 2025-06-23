package com.ajaxjs.api.limit.leakbucket;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 漏桶限流
 * <a href="https://blog.51cto.com/u_15127667/4523310">...</a>
 */
public final class LeakyBucket {
    /**
     * 桶的容量
     */
    private int capacity = 10;

    /**
     * 木桶剩余的水滴的量(初始化的时候的空的桶)
     */
    private final AtomicInteger water = new AtomicInteger(0);

    /**
     * 水滴的流出的速率 每1000毫秒流出1滴
     */
    private final int leakRate;

    /**
     * 第一次请求之后,木桶在这个时间点开始漏水
     */
    private long leakTimeStamp;

    public LeakyBucket(int capacity, int leakRate) {
        this.capacity = capacity;
        this.leakRate = leakRate;
    }

    public LeakyBucket(int leakRate) {
        this.leakRate = leakRate;
    }

    public boolean acquire() {
        // 如果是空桶，就当前时间作为桶开是漏出的时间
        if (water.get() == 0) {
            leakTimeStamp = System.currentTimeMillis();
            water.addAndGet(1);

            return capacity != 0;
        }

        // 先执行漏水，计算剩余水量
        int waterLeft = water.get() - ((int) ((System.currentTimeMillis() - leakTimeStamp) / 1000)) * leakRate;
        water.set(Math.max(0, waterLeft));
        // 重新更新leakTimeStamp
        leakTimeStamp = System.currentTimeMillis();

        // 尝试加水,并且水还未满
        if ((water.get()) < capacity) {
            water.addAndGet(1);

            return true;
        } else
            return false;// 水满，拒绝加水
    }
}
