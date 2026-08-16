package com.ajaxjs.framework.cache.delayqueue;

import com.ajaxjs.framework.cache.Cache;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可自动移除过期的缓存项
 * DelayQueue是一个无界的BlockingQueue，用于放置实现了Delayed接口的对象，其中的对象只能在其到期时才能从队列中取走。这种队列是有序的，即队头对象的延迟到期时间最长。注意：不能将null元素放置到这种队列中。
 * 因为DelayQueue是基于PriorityQueue实现的,PriorityQueue底层是一个堆,可以按时间排序，所以等待队列本身只需要维护根节点的一个定时器就可以了，而且插入和删除都是时间复杂度都是logN，资源消耗很少，作为一个缓存的定时装置是非常适合的
 * 使用 DelayQueue 需要传入一个Delayed对象，要实现两个方法
 * 1 getDelay(TimeUnit unit) 表示当前对象关联的延时，在本例中表示生存时间
 * 2 compareTo(Delayed o) 堆在进行删除和插入时需要进行对比，所以要传入一个比较器
 * <a href="https://www.jianshu.com/p/28d3efa0c1d0">...</a>
 */
@Slf4j
public class ExpiryCache<K, V> implements Cache<K, V> {
    /**
     * 使用阻塞队列中的等待队列，因为 DelayQueue 是基于 PriorityQueue 实现的，而 PriorityQueue 底层是一个最小堆，可以按过期时间排序，
     * 所以等待队列本身只需要维护根节点的一个定时器就可以了，而且插入和删除都是时间复杂度都是 logN，资源消耗很少
     */
    private final DelayQueue<ExpiryCacheItem<K>> DELAY = new DelayQueue<>();

    /**
     * 键值对集合
     */
    private final Map<K, V> CACHE = new ConcurrentHashMap<>();

    private final AtomicInteger size = new AtomicInteger(0);

    private volatile boolean valid = true;

    public ExpiryCache() {
        // 生成一个线程扫描等待队列的值
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            while (valid) {
                try {
                    ExpiryCacheItem<K> item = DELAY.take();// 此方法是阻塞的，没有到过期时间就阻塞在这里，直到取到数据
                    CACHE.remove(item.getValue());
                    size.decrementAndGet();

                    log.info("缓存项已过期! key: {}", item.getValue());
                } catch (InterruptedException e) {
                    log.warn("ExpiryMap 线程被打断", e);
                }
            }
        });
    }

    /**
     * 向缓存中添加键值对，并设置过期时间。
     *
     * @param key    键
     * @param data   值
     * @param expire 过期时间，单位为毫秒
     */
    @Override
    public void put(K key, V data, long expire) {
        CACHE.put(key, data);

        // 当等于0时，就不把过期时间放进队列里了，值在代码运行期间会一直存在
        if (expire != 0)
            DELAY.offer(new ExpiryCacheItem<>(key, System.currentTimeMillis() + expire));

        size.incrementAndGet();
        log.info("添加缓存项。key: {}, value: {}", key, data);
    }

    @Override
    public V get(K key) {
        return CACHE.get(key);
    }

    @Override
    public void remove(K key) {
        CACHE.remove(key);
    }

    /**
     * 清空缓存
     */
    public void clear() {
        size.compareAndSet(size.get(), 0); // 将缓存大小设为 0
        valid = false; // 将 valid 标志设为 false
        DELAY.clear(); // 清空延迟队列
        CACHE.clear(); // 清空缓存
    }

    private static volatile ExpiryCache<String, Object> INSTANCE;

    /**
     * 获取 ExpiryCache 的单例实例
     * 使用单例模式，加上双重验证，可适用于多线程高并发情况
     *
     * @return ExpiryCache 的单例实例
     */
    public static ExpiryCache<String, Object> getInstance() {
        if (INSTANCE == null)
            synchronized (ExpiryCache.class) {
                if (INSTANCE == null)
                    INSTANCE = new ExpiryCache<>();
            }

        return INSTANCE;
    }
}