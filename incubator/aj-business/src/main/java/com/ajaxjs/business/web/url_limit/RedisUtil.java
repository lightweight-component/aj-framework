//package com.ajaxjs.business.web.url_limit;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.script.DefaultRedisScript;
//import org.springframework.data.redis.core.script.RedisScript;
//
//import java.util.Collections;
//
//public class RedisUtil {
//    private static final Long SUCCESS = 1L;
//
//    @Autowired
//    public RedisTemplate<String, Object> redisTemplate;
//    // =============================common============================
//
//    /**
//     * 获取锁
//     *
//     * @param expireTime：单位-
//     */
//    public boolean getLock(String lockKey, Object value, int expireTime) {
//        try {
////            log.info("添加分布式锁key={},expireTime={}",lockKey,expireTime);
//            String script = "if redis.call('setNx',KEYS[1],ARGV[1]) then if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('expire',KEYS[1],ARGV[2]) else return 0 end end";
//            RedisScript<String> redisScript = new DefaultRedisScript<>(script, String.class);
//            Object result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), value, expireTime);
//
//            if (SUCCESS.equals(result))
//                return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return false;
//    }
//
//    /**
//     * 释放锁
//     */
//    public boolean releaseLock(String lockKey, String value) {
//        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//        RedisScript<String> redisScript = new DefaultRedisScript<>(script, String.class);
//        Object result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), value);
//
//        return SUCCESS.equals(result);
//    }
//}
