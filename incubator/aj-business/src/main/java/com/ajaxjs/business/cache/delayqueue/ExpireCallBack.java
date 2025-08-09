package com.ajaxjs.business.cache.delayqueue;

public interface ExpireCallBack<K, V> {
    void handler(K key, boolean isEnd) throws Exception;

}