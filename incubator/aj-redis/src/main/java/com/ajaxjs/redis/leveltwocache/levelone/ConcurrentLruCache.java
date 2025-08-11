package com.ajaxjs.redis.leveltwocache.levelone;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantReadWriteLock;
//import java.util.function.Function;

/**
 * 修改自 Spring，调整 put 设置缓存，不是从 Function 获取，而是提供一个 put 方法
 * <p>Cache 可否改为改为普通的 HashMap？
 * <a href="https://tianshuang.me/2021/12/ConcurrentLruCache/">...</a>
 * <a href="https://stackoverflow.com/questions/70430066/why-does-concurrentlrucache-in-spring-still-use-thread-safe-maps-and-queues-inst">...</a>
 *
 * @param <K>
 * @param <V>
 */
public class ConcurrentLruCache<K, V> {
    /**
     * 缓存总容量
     */
    private final int sizeLimit;

//    private final Function<K, V> generator;

    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

    /**
     * 当前实现为队列尾部为最近访问的节点
     * p.s 一般 LRU 会用链表做，而塔这里用了队列
     */
    private final ConcurrentLinkedDeque<K> queue = new ConcurrentLinkedDeque<>();

    /**
     * 可重入的读写锁
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 当前缓存容量
     * 对 size 的读取并不都被锁保护，所以使用了 volatile 修饰保证可见性
     */
    private volatile int size;

    /**
     * Create a new cache instance with the given limit and generator function.
     *
     * @param sizeLimit the maximum number of entries in the cache (0 indicates no caching, always generating a new value)
     */
    public ConcurrentLruCache(int sizeLimit/*, Function<K, V> generator*/) {
        if (sizeLimit < 0)
            throw new IllegalArgumentException("Cache size limit must not be negative");
//
//        if (generator == null)
//            throw new NullPointerException("Generator function must not be null");

        this.sizeLimit = sizeLimit;
//        this.generator = generator;
    }

    /**
     * Retrieve an entry from the cache, potentially triggering generation of the value.
     *
     * @param key the key to retrieve the entry for
     * @return the cached or newly generated value
     */
    public V get(K key) {
        V cached = cache.get(key);

        if (cached != null) {// 如果存在 key 对应的缓存
            if (size < sizeLimit) // 如果当前缓存区中缓存项个数不足 sizeLimit, 说明无需移动 queue 中的节点，直接返回即可
                return cached;

            lock.readLock().lock(); // 执行到此处说明缓存区中元素个数已经大于等于 sizeLimit, 那么此时需要移动 queue 中的节点

            try {
                if (queue.removeLastOccurrence(key))// 从队列尾部向前找当前访问的 key 并移除
                    queue.offer(key);// 将当前访问的 key 加入至队列尾部，注意此操作在读锁中实现，即存在并发调用，所以 queue 采用了线程安全的实现：ConcurrentLinkedDeque

                return cached;
            } finally {
                lock.readLock().unlock();
            }
        }

        return null;

//        this.lock.writeLock().lock();
//        try {
//            // Retrying in case of concurrent reads on the same key
//            cached = this.cache.get(key);
//
//            if (cached != null) {
//                if (this.queue.removeLastOccurrence(key))
//                    this.queue.offer(key);
//
//                return cached;
//            }
//            // Generate value first, to prevent size inconsistency
//            V value = this.generator.apply(key);
//            if (this.size == this.sizeLimit) {
//                K leastUsed = this.queue.poll();
//
//                if (leastUsed != null)
//                    this.cache.remove(leastUsed);
//            }
//            this.queue.offer(key);
//            this.cache.put(key, value);
//            this.size = this.cache.size();
//
//            return value;
//        } finally {
//            this.lock.writeLock().unlock();
//        }
    }

    /**
     * Manually add an entry into the cache.
     *
     * @param key   the key to associate the value with
     * @param value the value to cache
     */
    public void put(K key, V value) {
        lock.writeLock().lock();

        try {
            if (this.size == this.sizeLimit) {// 如果缓存区中的缓存项个数已经达到预设的个数限制，则移除队列头部元素
                K leastUsed = this.queue.poll();

                if (leastUsed != null)
                    this.cache.remove(leastUsed);
            }

            this.queue.offer(key);// 添加至队列尾部，即最近的元素在队尾
            this.cache.put(key, value);
            this.size = this.cache.size();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Determine whether the given key is present in this cache.
     *
     * @param key the key to check for
     * @return {@code true} if the key is present,
     * {@code false} if there was no matching key
     */
    public boolean contains(K key) {
        return cache.containsKey(key);
    }

    /**
     * Immediately remove the given key and any associated value.
     *
     * @param key the key to evict the entry for
     * @return {@code true} if the key was present before,
     * {@code false} if there was no matching key
     */
    public boolean remove(K key) {
        lock.writeLock().lock();

        try {
//            boolean wasPresent = (cache.remove(key) != null);
            queue.remove(key);
            size = cache.size();
            return cache.remove(key) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Immediately remove all entries from this cache.
     */
    public void clear() {
        this.lock.writeLock().lock();

        try {
            this.cache.clear();
            this.queue.clear();
            this.size = 0;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Return the current size of the cache.
     *
     * @see #sizeLimit()
     */
    public int size() {
        return this.size;
    }

    /**
     * Return the maximum number of entries in the cache (0 indicates no caching, always generating a new value).
     *
     * @see #size()
     */
    public int sizeLimit() {
        return this.sizeLimit;
    }
}