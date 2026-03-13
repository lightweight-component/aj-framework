package com.ajaxjs.framework.cache.lru;

import com.ajaxjs.framework.cache.CacheItem;

/**
 * 缓存值包装类，包含值和过期时间
 */
public class LRUCacheItem<V> extends CacheItem<V> {
    public LRUCacheItem(V value, long ttlMillis) {
        super(value, ttlMillis > 0 ? System.currentTimeMillis() + ttlMillis : Long.MAX_VALUE);
    }

    boolean isExpired() {
        return System.currentTimeMillis() > getExpire();
    }
}
