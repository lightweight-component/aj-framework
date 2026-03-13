package com.ajaxjs.framework.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 被缓存的数据
 *
 * @param <V> 缓存数据的类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheItem<V> {
    /**
     * 缓存值
     */
    private V value;

    /**
     * 到期时间（毫秒）
     */
    private long expire;
}
