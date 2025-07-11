package com.ajaxjs.spring.cache.springcache;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;

public class ConcurrentLruSpringCache implements Cache {
    private final String name;
    private final SimpleLRUCache<Object, Object> lruCache;

    public ConcurrentLruSpringCache(String name, int maxSize) {
        this.name = name;
        this.lruCache = new SimpleLRUCache<>(maxSize);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this.lruCache;
    }

    @Override
    public ValueWrapper get(Object key) {
        Object value = lruCache.get(key);
        return value != null ? new SimpleValueWrapper(value) : null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        Object value = lruCache.get(key);

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
        lruCache.put(key, value);
    }

    @Override
    public void evict(Object key) {
        lruCache.remove(key);
    }

    @Override
    public void clear() {
        lruCache.clear();
    }
}