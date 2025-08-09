package com.ajaxjs.framework.cache.lfu;

import com.ajaxjs.framework.cache.Cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO TreeMap 和 LinkedHashSet 不是线程安全的，所以需要加锁或使用 ConcurrentSkipListMap
 *
 * @param <K>
 * @param <V>
 */
public class LFUCache<K, V> implements Cache<K, V> {
    private final ConcurrentHashMap<K, LFUCacheItem<V>> cache = new ConcurrentHashMap<>();
    private final TreeMap<Integer, LinkedHashSet<K>> freqToKeys = new TreeMap<>();

    //     替代 TreeMap<Integer, LinkedHashSet<K>>=new TreeMap<>();
//    private final ConcurrentSkipListMap<Integer, KeySet> freqToKeys = new ConcurrentSkipListMap<>();
    private final int capacity;

    /**
     * 默认 TTL（毫秒），0 表示无默认
     */
    private final long defaultTtlMillis;
    private final AtomicInteger size = new AtomicInteger(0);

    // 后台清理相关
    private final boolean enableExpiryCleanup;

    private final long cleanupIntervalMs = 1000L;

    private ScheduledExecutorService cleanupExecutor;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public LFUCache(int capacity) {
        this(capacity, 0, false); // 无默认 TTL
    }

    public LFUCache(int capacity, long defaultTtlSeconds, boolean enableExpiryCleanup) {
        if (capacity <= 0)
            throw new IllegalArgumentException("Capacity must be positive");

        this.capacity = capacity;
        this.defaultTtlMillis = defaultTtlSeconds * 1000L;
        this.enableExpiryCleanup = enableExpiryCleanup;

        if (enableExpiryCleanup) {
            cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "LFU-Cache-Expiry-Cleaner");
                t.setDaemon(true); // 守护线程

                return t;
            });
            startExpiryCleanupTask();
        }
    }

    /**
     * 获取值，检查是否过期
     */
    @Override
    public V get(K key) {
        LFUCacheItem<V> entry = cache.get(key);

        if (entry == null || isExpired(entry)) {
            // 如果过期，清理
            if (entry != null) {
                removeFromFreqMap(entry.getFrequency(), key);
                cache.remove(key);
                size.decrementAndGet();
            }

            return null;
        }

        increaseFrequency(key, entry);
        return entry.getValue();
    }

    /**
     * put：使用默认 TTL（如果设置了）
     */
    @Override
    public void put(K key, V value) {
        if (defaultTtlMillis > 0)
            put(key, value, defaultTtlMillis);
        else
            put(key, value, 0); // 0 表示永不过期
    }

    @Override
    public void put(K key, V value, long ttl) {
        put(key, value, (int) (ttl / 1000));
    }

    /**
     * put：指定 TTL（单位：秒）
     * ttlSeconds = 0 表示永不过期
     */
    @Override
    public void put(K key, V value, int ttlSeconds) {
        long expireTime = (ttlSeconds > 0) ? (System.currentTimeMillis() + ttlSeconds * 1000L) : Long.MAX_VALUE;
        LFUCacheItem<V> oldEntry = cache.get(key);

        if (oldEntry != null && !isExpired(oldEntry)) {
            V oldValue = oldEntry.getValue();
            oldEntry.setValue(value);
            oldEntry.setExpire(expireTime);
            increaseFrequency(key, oldEntry);
        }

        // 处理新增
        if (size.get() >= capacity)
            evict(); // 淘汰一个条目

        LFUCacheItem<V> newEntry = new LFUCacheItem<>(value, 1, expireTime);
        cache.put(key, newEntry);
        freqToKeys.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
        size.incrementAndGet();
    }

    /**
     * 检查是否过期
     */
    private boolean isExpired(LFUCacheItem<V> entry) {
        return System.currentTimeMillis() > entry.getExpire();
    }

    /**
     * 增加频率
     */
    private void increaseFrequency(K key, LFUCacheItem<V> entry) {
        if (isExpired(entry)) { // 理论上不会到这里，但安全起见
            evictEntry(key, entry);
            return;
        }

        int oldFreq = entry.getFrequency();
        int newFreq = oldFreq + 1;
        entry.setFrequency(newFreq);

        removeFromFreqMap(oldFreq, key);
        freqToKeys.computeIfAbsent(newFreq, k -> new LinkedHashSet<>()).add(key);
    }

    /**
     * 淘汰一个条目
     * 优先淘汰已过期的条目，否则淘汰频率最低的
     */
    private void evict() {
        // 先尝试找一个过期的条目淘汰
        for (Map.Entry<K, LFUCacheItem<V>> entry : cache.entrySet()) {
            if (isExpired(entry.getValue())) {
                evictEntry(entry.getKey(), entry.getValue());
                return;
            }
        }

        // 没有过期的，淘汰频率最低的（FIFO）
        Integer minFreq = freqToKeys.firstKey();
        LinkedHashSet<K> candidates = freqToKeys.get(minFreq);
        K keyToEvict = candidates.iterator().next();

        LFUCacheItem<V> entry = cache.get(keyToEvict);
        evictEntry(keyToEvict, entry);
    }

    /**
     * 统一淘汰逻辑
     */
    private void evictEntry(K key, LFUCacheItem<V> entry) {
        removeFromFreqMap(entry.getFrequency(), key);
        cache.remove(key);
        size.decrementAndGet();
    }

    /**
     * 从缓存中删除对象
     *
     * @param key 键
     */
    public void remove(K key) {
        LFUCacheItem<V> entry = cache.get(key);

        if (entry != null)
            evictEntry(key, entry);
    }

    /**
     * 从 freqToKeys 中移除 key
     */
    private void removeFromFreqMap(int freq, K key) {
        LinkedHashSet<K> set = freqToKeys.get(freq);

        if (set != null) {
            set.remove(key);

            if (set.isEmpty())
                freqToKeys.remove(freq);
        }
    }

    public int size() {
        return size.get();
    }

    public void clear() {
        cache.clear();
        freqToKeys.clear();
        size.set(0);
    }

    // ===================== 后台清理任务 =====================
    private void startExpiryCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(() -> {
            if (closed.get())
                return;

            try {
                List<K> expiredKeys = new ArrayList<>();
                // 扫描所有条目，收集过期的 key
                for (Map.Entry<K, LFUCacheItem<V>> entry : cache.entrySet()) {
                    if (isExpired(entry.getValue()))
                        expiredKeys.add(entry.getKey());
                }

                // 批量清理
                for (K key : expiredKeys)
                    evictEntry(key, cache.get(key));

            } catch (Exception e) {
                // 防止任务因异常退出
                System.err.println("Expiry cleanup task error: " + e.getMessage());
            }
        }, cleanupIntervalMs, cleanupIntervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 关闭缓存，释放后台线程
     */
    public void close() {
        if (closed.compareAndSet(false, true) && enableExpiryCleanup) {
            cleanupExecutor.shutdown();

            try {
                if (!cleanupExecutor.awaitTermination(3, TimeUnit.SECONDS))
                    cleanupExecutor.shutdownNow();
            } catch (InterruptedException e) {
                cleanupExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

    }
}