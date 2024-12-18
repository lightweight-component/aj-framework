//package com.ajaxjs.util.cache.mixredis;
//
//import org.springframework.data.redis.cache.RedisCache;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.core.RedisOperations;
//
//import java.util.EnumMap;
//
//public class MyRedisCacheManager extends RedisCacheManager {
//    public MyRedisCacheManager(RedisOperations redisOperations) {
//        super(redisOperations);
//    }
//
//    @Override
//    protected RedisCache createCache(String cacheName) {
//        EnumMap f;
//        long expiration = computeExpiration(cacheName);
//        return new MyRedisCache(this, cacheName, (isUsePrefix() ? getCachePrefix().prefix(cacheName) : null), getRedisOperations(), expiration);
//    }
//
//    /**
//     * get a messsage for update cache
//     *
//     * @param cacheName
//     */
//    public void receiver(String cacheName) {
//        MyRedisCache cache = (MyRedisCache) this.getCache(cacheName);
//        if (cache == null)
//            return;
//
//        cache.cacheUpdate();
//
//    }
//
//    //notify other redis clent to update cache( clear local cache in fact)
//    public void publishMessage(String cacheName) {
//        getRedisOperations().convertAndSend(topicName, cacheName);
//    }
//}
