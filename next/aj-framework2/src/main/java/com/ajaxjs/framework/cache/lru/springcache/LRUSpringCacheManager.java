package com.ajaxjs.framework.cache.lru.springcache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  @Bean
 *     public CacheManager cacheManager() {
 *         // LRU 容量为 100，可自行调整
 *         return new ConcurrentLruCacheManager(100);
 *     }
 */
public class LRUSpringCacheManager implements CacheManager {
    private final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();
    private final int maxSize;

    public LRUSpringCacheManager(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, n -> new LRUSpringCache(n, maxSize));
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(cacheMap.keySet());
    }
}