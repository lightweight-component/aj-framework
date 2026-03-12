package com.ajaxjs.framework.cache.lfu.springcache;

import com.ajaxjs.framework.cache.lfu.LFUCache;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;

public class LFUSpringCache implements Cache {
    private final String name;

    private final LFUCache<String, Object> lfuCache;

    public LFUSpringCache(String name, int maxSize) {
        this.name = name;
        this.lfuCache = new LFUCache<>(maxSize);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return lfuCache;
    }

    @Override
    public ValueWrapper get(Object key) {
        Object value = lfuCache.get(key.toString());
        System.out.println("LFU Cache: " + value);

        return value != null ? new SimpleValueWrapper(value) : null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        Object value = lfuCache.get(key.toString());

        if (type != null && type.isInstance(value))
            return type.cast(value);

        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {
        lfuCache.put(key.toString(), value);
    }

    @Override
    public void evict(Object key) {
        lfuCache.remove(key.toString());
    }

    @Override
    public void clear() {
        lfuCache.clear();
    }
}
