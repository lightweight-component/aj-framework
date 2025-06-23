package com.ajaxjs.util.cache.leveltwocache;

public interface LevelOneCache<K, V> {
    V get(K key);

    void put(K key, V value);

    void evict(K key);

    void clear();
}