package com.ajaxjs.security.antireptile.util;

import com.ajaxjs.framework.cache.lru.LRUCache;

import java.time.Clock;
import java.util.Objects;

/**
 * 基于 AJAXJS LRUCache 的组件私有单机缓存。
 * <p>所有 TTL 明确使用毫秒 long 重载，避免 Cache 的 int 重载将其解释为秒。
 * 访问时再次校验截止时间，保证到期时刻即失效；过期条目由访问或容量淘汰回收。
 * 无后台线程。容量不足时按 LRU 淘汰，状态仅属于当前应用实例，重启即清空。</p>
 */
public class LocalCache {
    private final LRUCache<String, Entry> cache;

    private final Clock clock;

    /**
     * 使用系统时钟创建缓存。
     *
     * @param capacity 最大条目数，必须大于零
     */
    public LocalCache(int capacity) {
        this(capacity, Clock.systemUTC());
    }

    /**
     * 使用指定时钟创建缓存，便于确定性验证过期边界。
     *
     * @param capacity 最大条目数
     * @param clock    判断过期使用的时钟
     */
    public LocalCache(int capacity, Clock clock) {
        if (capacity <= 0)
            throw new IllegalArgumentException("Cache capacity must be positive");

        this.cache = new LRUCache<>(capacity);
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * 保存值并设置 TTL，覆盖已有条目时重新计时。
     *
     * @param key       组件内部带命名空间的键
     * @param value     缓存值
     * @param ttlMillis 有效期，单位毫秒，必须大于零
     */
    public synchronized void put(String key, Object value, long ttlMillis) {
        if (ttlMillis <= 0)
            throw new IllegalArgumentException("Cache TTL must be positive");

        cache.put(key, new Entry(value, Math.addExact(clock.millis(), ttlMillis)), ttlMillis);
    }

    /**
     * 获取尚未到期的值，读取不续期。
     *
     * @param key  缓存键
     * @param type 值的类型
     * @param <T>  值的类型参数
     * @return 缓存值；不存在或到期时为 null
     */
    public synchronized <T> T get(String key, Class<T> type) {
        Entry entry = cache.get(key);
        if (entry == null)
            return null;

        if (clock.millis() >= entry.expiresAt) {
            cache.remove(key);
            return null;
        }

        return type.cast(entry.value);
    }

    /**
     * @param key 要立即移除的缓存键
     */
    public synchronized void remove(String key) {
        cache.remove(key);
    }

    /**
     * 移除指定命名空间的条目，用于 IP 锁定时清除该 IP 的所有旧窗口。
     *
     * @param prefix 缓存键前缀
     */
    public synchronized void removeByPrefix(String prefix) {
        for (String key : cache.keySet()) {
            if (key.startsWith(prefix)) {
                cache.remove(key);
            }
        }
    }

    /**
     * 清空当前实例缓存，由 Spring 销毁 Bean 时调用。
     */
    public synchronized void clear() {
        cache.clear();
    }

    private static class Entry {
        private final Object value;

        private final long expiresAt;

        private Entry(Object value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
}
