package com.ajaxjs.redis.leveltwocache.levelone;

import com.ajaxjs.redis.leveltwocache.LevelOneCache;
import org.springframework.cache.Cache.ValueWrapper;

import java.util.concurrent.ConcurrentHashMap;
public class LevelOneSimpleCache implements LevelOneCache<String, ValueWrapper> {
    private final ConcurrentHashMap<String, ValueWrapper> cache = new ConcurrentHashMap<>();

    @Override
    public ValueWrapper get(String key) {
        return cache.get(key);
    }

    @Override
    public void put(String key, ValueWrapper value) {
        cache.put(key, value);
    }

    @Override
    public void evict(String key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }
}