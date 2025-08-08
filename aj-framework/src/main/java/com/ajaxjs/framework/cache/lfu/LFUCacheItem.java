package com.ajaxjs.framework.cache.lfu;

import com.ajaxjs.framework.cache.CacheItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LFUCacheItem<V> extends CacheItem<V> {
    private int frequency;

    public LFUCacheItem(V value, int frequency, long expire) {
        super(value, expire);
        this.frequency = frequency;
    }
}
