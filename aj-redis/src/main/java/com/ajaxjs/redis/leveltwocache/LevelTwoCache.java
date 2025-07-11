package com.ajaxjs.redis.leveltwocache;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

public class LevelTwoCache extends RedisCache {
    public LevelTwoCache(LevelTwoCacheManager cacheManager, LevelOneCache<String, ValueWrapper> local, String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfig) {
        this(name, cacheWriter, cacheConfig);
        this.cacheManager = cacheManager;
        this.local = local;
    }

    protected LevelTwoCache(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfig) {
        super(name, cacheWriter, cacheConfig);
    }

    private LevelTwoCacheManager cacheManager;

    private LevelOneCache<String, ValueWrapper> local;

//    private final ConcurrentHashMap<Object, ValueWrapper> local = new ConcurrentHashMap<>();

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper wrapper = local.get(key.toString());

        if (wrapper != null)
            return wrapper;
        else {
            wrapper = super.get(key);

            if (wrapper != null)
                local.put(key.toString(), wrapper);// 回填 L2 缓存

            return wrapper;
        }
    }

    @Override
    public void put(Object key, Object value) {
        super.put(key, value);
        cacheManager.publishMessage(super.getName());
    }

    @Override
    public void evict(Object key) {
        super.evict(key);
        cacheManager.publishMessage(super.getName());
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper wrapper = super.putIfAbsent(key, value);
        cacheManager.publishMessage(super.getName());

        return wrapper;
    }

    /**
     * Clear all cache for simplification
     */
    public void cacheUpdate() {
        local.clear();
    }
}
