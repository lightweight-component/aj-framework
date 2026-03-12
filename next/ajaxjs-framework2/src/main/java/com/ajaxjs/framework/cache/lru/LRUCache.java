package com.ajaxjs.framework.cache.lru;

import com.ajaxjs.framework.cache.Cache;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 带有超时功能的线程安全 LRU 缓存
 * 支持最大容量和每个缓存项的 TTL（存活时间）
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class LRUCache<K, V> implements Cache<K, V> {
    private final Map<K, LRUCacheItem<V>> cache;

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private final Lock lock = new ReentrantLock();

    private final long defaultTtlMillis;

    /**
     * 构造函数
     *
     * @param maxCapacity      最大容量
     * @param defaultTtlMillis 默认过期时间（毫秒），<=0 表示永不过期
     */
    public LRUCache(int maxCapacity, long defaultTtlMillis) {
        this.defaultTtlMillis = defaultTtlMillis;

        // 使用 LinkedHashMap 实现 LRU，accessOrder=true 表示按访问顺序排序
        cache = new LinkedHashMap<K, LRUCacheItem<V>>(maxCapacity, DEFAULT_LOAD_FACTOR, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, LRUCacheItem<V>> eldest) {
                return size() > maxCapacity;
            }
        };
    }

    public LRUCache(int maxCapacity) {
        this(maxCapacity, 0);// 默认不过期
    }

    /**
     * 获取值，若已过期则返回 null 并删除
     */
    @Override
    public V get(K key) {
        try {
            lock.lock();
            LRUCacheItem<V> cacheValue = cache.get(key);

            if (cacheValue == null)
                return null;

            if (cacheValue.isExpired()) {
                cache.remove(key);
                return null;
            }

            return cacheValue.getValue();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 存入缓存，使用默认 TTL
     */
    @Override
    public void put(K key, V value) {
        put(key, value, defaultTtlMillis);
    }

    /**
     * 存入缓存，并指定 TTL（毫秒）
     */
    @Override
    public void put(K key, V value, long ttlMillis) {
        try {
            lock.lock();
            cache.put(key, new LRUCacheItem<>(value, ttlMillis));
        } finally {
            lock.unlock();
        }
    }

    /**
     * 移除指定键
     */
    @Override
    public void remove(K key) {
        try {
            lock.lock();
            cache.remove(key);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 是否包含 key（且未过期）
     */
    public boolean containsKey(K key) {
        try {
            lock.lock();
            LRUCacheItem<V> cv = cache.get(key);

            if (cv != null) {
                if (cv.isExpired()) {
                    cache.remove(key);

                    return false;
                }

                return true;
            }

            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 当前有效大小（清理过期项）
     */
    public int size() {
        try {
            lock.lock();
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired()); // 清理过期项

            return cache.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        try {
            lock.lock();
            cache.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前缓存中所有未过期的 key 集合
     */
    public Set<K> keySet() {
        try {
            lock.lock();
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());

            return new HashSet<>(cache.keySet());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前缓存中所有未过期的值集合
     */
    public Collection<V> values() {
        try {
            lock.lock();
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            List<V> values = new ArrayList<>();

            for (LRUCacheItem<V> cv : cache.values())
                values.add(cv.getValue());

            return values;
        } finally {
            lock.unlock();
        }
    }
}