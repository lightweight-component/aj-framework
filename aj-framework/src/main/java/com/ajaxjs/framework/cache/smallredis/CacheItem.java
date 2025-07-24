package com.ajaxjs.framework.cache.smallredis;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 被缓存的数据
 *
 * @param <V> 缓存数据的类型
 */
@Data
@AllArgsConstructor
public class CacheItem<V> {
    /**
     * 缓存值
     */
    private V value;

    /**
     * 到期时间
     */
    private long expire;
}
