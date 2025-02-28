package com.ajaxjs.util.cache.leveltwocache.levelone;

import com.ajaxjs.util.cache.leveltwocache.LevelOneCache;
import org.springframework.cache.Cache;


public class LevelOneLruCache implements LevelOneCache<String, Cache.ValueWrapper> {
    ConcurrentLruCache<String, Cache.ValueWrapper> map = new ConcurrentLruCache<>(10);

    @Override
    public Cache.ValueWrapper get(String key) {
        return map.get(key);
    }

    @Override
    public void put(String key, Cache.ValueWrapper value) {
//        map.add(key, value);
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
