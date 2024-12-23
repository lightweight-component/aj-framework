package com.ajaxjs.util.cache.leveltwocache.levelone;

import com.ajaxjs.util.cache.leveltwocache.LevelOneCache;
import org.springframework.cache.Cache;
import org.springframework.util.ConcurrentLruCache;

public class LevelOneLruCache implements LevelOneCache<String, Cache.ValueWrapper> {
    ConcurrentLruCache<String, Cache.ValueWrapper> map = new ConcurrentLruCache<>(10, null);

    @Override
    public Cache.ValueWrapper get(String key) {
        return map.get(key);
    }

    @Override
    public void put(String key, Cache.ValueWrapper value) {
        map.set(key, value);
    }

    @Override
    public void evict(String key) {
        map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }
}
