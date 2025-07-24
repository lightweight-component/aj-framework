package com.ajaxjs.redis.leveltwocache;

import com.ajaxjs.util.StrUtil;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

public class LevelTwoCacheManager extends RedisCacheManager {
    RedisTemplate<String, Object> redisTemplate;

    String topicName;

    RedisCacheWriter writer;

    RedisCacheConfiguration cacheConfig;

    private LevelOneCache<String, Cache.ValueWrapper> local;

    public LevelTwoCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
        super(cacheWriter, defaultCacheConfiguration);
        this.writer = cacheWriter;
        this.cacheConfig = defaultCacheConfiguration;
    }

    @Override
    protected RedisCache createRedisCache(String name, @Nullable RedisCacheConfiguration cacheConfig) {
//        return new LevelTwoCache(this, topicName + ":" + name, cacheWriter2, cacheConfig);
        return new LevelTwoCache(this, local, topicName, writer, cacheConfig);
    }

    public RedisCache createRedisCache() {
//        return new LevelTwoCache(this, topicName + ":" + name, cacheWriter2, defaultCacheConfiguration2);
        return new LevelTwoCache(this, local, topicName, writer, cacheConfig);
    }

    /**
     * get a message for update cache
     *
     * @param cacheName
     */
    public void receiver(String cacheName) {
        LevelTwoCache cache = (LevelTwoCache) getCache(cacheName);
        if (cache == null)
            return;

        cache.cacheUpdate();
    }

    //notify other redis client to update cache( clear local cache in fact)
    public void publishMessage(String cacheName) {
        redisTemplate.convertAndSend(topicName, cacheName);
    }

    public void put(String key, Object value) {
        createRedisCache().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clz) {
        Cache cache = getCache(StrUtil.EMPTY_STRING);
        if (cache == null)
            return null;

        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper == null)
            return null;

        Object v = valueWrapper.get();

        return (T) v;
    }

    public String getString(String key) {
        return get(key, String.class);
    }

    public Integer getInteger(String key) {
        return get(key, Integer.class);
    }

    public Long getLong(String key) {
        return get(key, Long.class);
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setLocal(LevelOneCache<String, Cache.ValueWrapper> local) {
        this.local = local;
    }
}
