package com.ajaxjs.framework.cache.delayqueue.another;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * DelayQueue 是一个支持延时获取元素的阻塞队列， 内部采用优先队列 PriorityQueue 存储元素，同时元素必须实现 Delayed 接口；在创建元素时可以指定多久才可以从队列中获取当前元素，只有在延迟期满时才能从队列中提取元素。
 * - 缓存系统 ： 当能够从 DelayQueue 中获取元素时，说该缓存已过期
 * - 定时任务调度
 * <a href="https://juejin.cn/post/6844903721390833678">...</a>
 */
public class DelayQueueCacheItem implements Delayed {
    private final String key;

    /**
     * 过期时间(单位秒)
     */
    private final long expireTime;

    private final long currentTime;

    public DelayQueueCacheItem(String key, long expireTime) {
        this.key = key;
        this.expireTime = expireTime;
        this.currentTime = System.currentTimeMillis();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        // 计算剩余的过期时间
        // 大于 0 说明未过期
        return expireTime - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - currentTime);
    }

    @Override
    public int compareTo(Delayed o) {
        // 过期时间长的放置在队列尾部
        if (getDelay(TimeUnit.MICROSECONDS) > o.getDelay(TimeUnit.MICROSECONDS))
            return 1;

        // 过期时间短的放置在队列头
        if (getDelay(TimeUnit.MICROSECONDS) < o.getDelay(TimeUnit.MICROSECONDS))
            return -1;

        return 0;
    }

    public String getKey() {
        return key;
    }
}