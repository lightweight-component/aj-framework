package com.ajaxjs.redis;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * Redisson 分布式锁
 * <a href="https://blog.csdn.net/a807719447/article/details/112463121">...</a>
 */
public class RedissonLock {
    public static final String LOCK_PREFIX = "RDL-";

    public static RLock getLock(RedissonClient redissonClient, String uid) {
        return redissonClient.getLock(LOCK_PREFIX + uid);
    }

    /**
     * @param lock      Redisson Lock
     * @param waitTime  the maximum time to acquire the lock
     * @param leaseTime lease time
     */
    public static void tryLock(RLock lock, long waitTime, long leaseTime) {
        try {
            lock.tryLock(5, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Error when adding a lock", e);
        }
    }

}
