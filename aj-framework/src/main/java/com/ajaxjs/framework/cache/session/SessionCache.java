package com.ajaxjs.framework.cache.session;

import com.ajaxjs.framework.cache.Cache;
import com.ajaxjs.framework.cache.CacheItem;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionCache implements Cache<String, Object> {
    private static final String CACHE_MAP_KEY = "_SESSION_CACHE_MAP_";

    private Map<String, CacheItem<Object>> cacheMap;

    @SuppressWarnings("unchecked")
    public SessionCache(HttpSession session) {
        cacheMap = (Map<String, CacheItem<Object>>) session.getAttribute(CACHE_MAP_KEY);
        // 从 Session 中获取或创建缓存映射
        if (cacheMap == null) {
            cacheMap = new ConcurrentHashMap<>();
            session.setAttribute(CACHE_MAP_KEY, cacheMap);
        }
    }

    @Override
    public void put(String key, Object value, long timeout) {
        if (value == null) {
            remove(key);
            return;
        }

        long expire = timeout <= 0 ? 0 : (System.currentTimeMillis() + timeout);
        CacheItem<Object> item = new CacheItem<>(value, expire);
        cacheMap.put(key, item);
    }

    @Override
    public Object get(String key) {
        CacheItem<Object> item = cacheMap.get(key);
        if (item == null)
            return null;

        // 检查是否过期
        if (item.getExpire() > 0 && System.currentTimeMillis() > item.getExpire()) {
            // 过期，自动清理
            cacheMap.remove(key);
            return null;
        }

        return item.getValue();
    }

    @Override
    public void remove(String key) {
        cacheMap.remove(key);
    }

    /**
     * 获取底层缓存映射（可用于清理或监控）
     */
    public Map<String, CacheItem<Object>> getCacheMap() {
        return cacheMap;
    }
}
