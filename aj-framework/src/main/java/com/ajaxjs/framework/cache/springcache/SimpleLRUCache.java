package com.ajaxjs.framework.cache.springcache;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 带锁的线程安全的 LRULinkedHashMap 简单实现
 *
 * @author <a href="https://blog.csdn.net/a921122/article/details/51992713">...</a>
 *
 * @param <K>
 * @param <V>
 */
public class SimpleLRUCache<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = -952299094512767664L;

    /**
     * 最大容量
     */
    private final int maxCapacity;

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 可重入锁
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 创建一个 LRUCache
     *
     * @param maxCapacity 最大容量
     */
    public SimpleLRUCache(int maxCapacity) {
        super(maxCapacity, DEFAULT_LOAD_FACTOR, true);
        this.maxCapacity = maxCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }

    @Override
    public V get(Object key) {
        try {
            lock.lock();
            return super.get(key);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 可以根据实际情况，考虑对不同的操作加锁
     */
    @Override
    public V put(K key, V value) {
        try {
            lock.lock();
            return super.put(key, value);
        } finally {
            lock.unlock();
        }
    }

}