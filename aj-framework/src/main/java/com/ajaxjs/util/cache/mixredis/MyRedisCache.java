package com.ajaxjs.util.cache.mixredis;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.core.RedisOperations;

import java.util.concurrent.ConcurrentHashMap;


public class MyRedisCache extends RedisCache {
    //local cache for performace
    ConcurrentHashMap<Object, ValueWrapper> local = new ConcurrentHashMap<>();
    MyRedisCacheManager cacheManager;

    public MyRedisCache(MyRedisCacheManager cacheManager, String name, byte[] prefix, RedisOperations<?, ?> redisOperations, long expiration) {

        super(name, prefix, redisOperations, expiration);
        this.cacheManager = cacheManager;
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper wrapper = local.get(key);

        if (wrapper != null)
            return wrapper;
        else {
            wrapper = super.get(key);
            if (wrapper != null)
                local.put(key, wrapper);

            return wrapper;
        }
    }

    @Override
    public void put(final Object key, final Object value) {
        super.put(key, value);
        cacheManager.publishMessage(super.getName());
    }

    @Override
    public void evict(Object key) {
        super.evict(key);
        cacheManager.publishMessage(super.getName());
    }


    @Override
    public ValueWrapper putIfAbsent(Object key, final Object value) {
        ValueWrapper wrapper = super.putIfAbsent(key, value);
        cacheManager.publishMessage(super.getName());

        return wrapper;
    }

    public void cacheUpdate() {
        //clear all cache for simplification
        local.clear();
    }
}


