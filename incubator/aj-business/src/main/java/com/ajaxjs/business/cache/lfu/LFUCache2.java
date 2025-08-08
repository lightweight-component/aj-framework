package com.ajaxjs.business.cache.lfu;

import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LFUCache2<K, V> {
    // 缓存主体：key -> CacheEntry
    private final ConcurrentHashMap<K, CacheEntry<V>> cache;

    // 频率映射：frequency -> keys that have this frequency
    // 用于快速找到 frequency 最小的 key
    private final TreeMap<Integer, LinkedHashSet<K>> freqToKeys;

    private final int capacity;
    private final AtomicInteger size = new AtomicInteger(0);

    public LFUCache2(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException("Capacity must be positive");

        this.capacity = capacity;
        this.cache = new ConcurrentHashMap<>();
        this.freqToKeys = new TreeMap<>();
    }

    /**
     * 获取值，同时增加访问频率
     */
    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry == null)
            return null;

        // 增加访问频率
        increaseFrequency(key, entry);
        return entry.value;
    }

    /**
     * 插入或更新值
     */
    public V put(K key, V value) {
        V oldValue = null;
        CacheEntry<V> oldEntry = cache.get(key);

        if (oldEntry != null) {
            oldValue = oldEntry.value;
            oldEntry.value = value;
            increaseFrequency(key, oldEntry);
            return oldValue;
        }

        // 新增 entry
        if (size.get() >= capacity)
            evict(); // 淘汰一个条目

        CacheEntry<V> newEntry = new CacheEntry<>(value, 1);
        cache.put(key, newEntry);
        freqToKeys.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
        size.incrementAndGet();

        return oldValue;
    }

    /**
     * 增加访问频率
     */
    private void increaseFrequency(K key, CacheEntry<V> entry) {
        int oldFreq = entry.frequency;
        int newFreq = oldFreq + 1;
        entry.frequency = newFreq;

        // 从旧频率集合中移除
        LinkedHashSet<K> oldSet = freqToKeys.get(oldFreq);
        oldSet.remove(key);
        if (oldSet.isEmpty())
            freqToKeys.remove(oldFreq);

        // 添加到新频率集合
        freqToKeys.computeIfAbsent(newFreq, k -> new LinkedHashSet<>()).add(key);
    }

    /**
     * 淘汰一个条目（频率最低，且最早插入的）
     */
    private void evict() {
        // 找到最小频率
        Integer minFreq = freqToKeys.firstKey();
        LinkedHashSet<K> candidates = freqToKeys.get(minFreq);

        K keyToEvict = candidates.iterator().next(); // LinkedHashSet 保证插入顺序，第一个是最先加入的
        candidates.remove(keyToEvict);

        if (candidates.isEmpty())
            freqToKeys.remove(minFreq);

        cache.remove(keyToEvict);
        size.decrementAndGet();
    }

    public int size() {
        return size.get();
    }

    public boolean isEmpty() {
        return size.get() == 0;
    }

    public void clear() {
        cache.clear();
        freqToKeys.clear();
        size.set(0);
    }

    // 缓存条目
    private static class CacheEntry<V> {
        V value;
        int frequency; // 访问频率

        public CacheEntry(V value, int frequency) {
            this.value = value;
            this.frequency = frequency;
        }
    }

    public static void main(String[] args) {
        LFUCache2<String, String> cache = new LFUCache2<>(3);

        cache.put("A", "Apple");
        cache.put("B", "Banana");
        cache.put("C", "Cherry");

        cache.get("A"); // freq: A=2, B=1, C=1
        cache.get("A"); // freq: A=3, B=1, C=1
        cache.get("B"); // freq: A=3, B=2, C=1

        // 插入 D，触发淘汰，C 被淘汰（频率最低）
        cache.put("D", "Date");

        System.out.println(cache.get("C")); // null
        System.out.println(cache.get("D")); // Date
    }
}