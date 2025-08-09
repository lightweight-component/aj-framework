package com.ajaxjs.business.cache.delayqueue;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ExpireCache<K, V> {

    //Data Cache
    private final ConcurrentHashMap<K, V> _CACHE_MAP = new ConcurrentHashMap<>();

    //DelayQueue for expire
    private final DelayQueue<DelayItem<K>> _Q = new DelayQueue<>();

    private Thread expireCheckThread;

    //首次超时时间
    private final long firstExpireTime;

    //超时之后是否删除(如果超时后继续,则把该对象再次放入cache,每隔subsequentExpireTime时间检查一次,直到 checkTimes 次数)
    private boolean continueCheckAfterExpire;

    //第一次超时之后，后续超时时间
    private long subsequentExpireTime;

    //超时单位
    private final TimeUnit unit;

    //总共检查次数
    private Integer checkTimes;

    private ExpireCallBack<K, V> expireCallBack;

    private ExpireCache(long firstExpireTime, TimeUnit unit) {
        this.firstExpireTime = firstExpireTime;
        this.unit = unit;
    }

    public static <K, V> ExpireCache<K, V> setExpireTime(long firstExpireTime, long subsequentExpireTime, TimeUnit unit, boolean continueCheckAfterExpire) {
        ExpireCache<K, V> expireCache = new ExpireCache<>(firstExpireTime, unit);

        if (continueCheckAfterExpire) {
            expireCache.continueCheckAfterExpire = true;

            if (subsequentExpireTime <= 0)
                expireCache.subsequentExpireTime = firstExpireTime;
            else
                expireCache.subsequentExpireTime = subsequentExpireTime;
        }

        return expireCache;
    }

    public ExpireCache<K, V> setCheckTimes(Integer times) {
        if (times != null && times > 0)
            this.checkTimes = times - 1;

        return this;
    }

    /**
     * 采用这种方式，而不是在构造方法中创建线程是为了防止构造方法中启动线程，且线程中会有实例引用导致的this逸出
     * <p>下面带参数的build方法原因也是如此
     */
    public ExpireCache<K, V> build() {
        expireCheckThread = new Thread(this::expireCheck);
        expireCheckThread.setDaemon(true);
        expireCheckThread.setName("ExpireCacheCheckThread");
        expireCheckThread.start();

        return this;
    }

    /**
     * 带有失效回调函数的build方法
     */
    public ExpireCache<K, V> build(ExpireCallBack<K, V> callBack) {
        this.expireCallBack = callBack;
        expireCheckThread = new Thread(this::expireCheck);
        expireCheckThread.setDaemon(true);
        expireCheckThread.setName("ExpireCacheCheckThread");
        expireCheckThread.start();

        return this;
    }

    /**
     * 真正的失效检测
     */
    private void expireCheck() {
        for (; ; ) {
            try {
                DelayItem<K> delayItem = _Q.take();

                if (delayItem != null) {
                    log.warn("[expireCache] timeout");

                    if (expireCallBack != null) {
                        try {
                            expireCallBack.handler(delayItem.getItem(), delayItem.isEnd());
                        } catch (Exception e) {
                            log.error("[ExpireCache expireCheck] callback error", e);
                        }
                    }

                    //如果超时后还继续检测，则item设置新的超时时间;并且没有超过总检查次数
                    //否则从Cache中删除数据
                    if (continueCheckAfterExpire && !delayItem.isEnd()) {
                        long milliseconds = TimeUnit.MILLISECONDS.convert(subsequentExpireTime, unit);
                        delayItem.setMilliseconds(milliseconds);

                        if (delayItem.getCheckTimesLeft() != null)
                            delayItem.setCheckTimesLeft(delayItem.getCheckTimesLeft() - 1);

                        _Q.put(delayItem);
                    } else
                        _CACHE_MAP.remove(delayItem.getItem());
                }
            } catch (InterruptedException e) {
                log.error("[Expire Cache] do expireCheckError", e);

                //失败后停100ms
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void put(K key, V value) {
        V oldValue = _CACHE_MAP.put(key, value);
        if (oldValue != null)
            //todo 这个地方性能比较差，DelayQueue删除元素慢
            _Q.remove(new DelayItem<>(key, 0L, 0));


        long milliseconds = TimeUnit.MILLISECONDS.convert(firstExpireTime, unit);
        DelayItem<K> delayItem = new DelayItem<>(key, milliseconds, checkTimes);

        _Q.put(delayItem);
    }

    public boolean expire(K key) {
        boolean rs = _CACHE_MAP.containsKey(key);

        if (rs) {
            long milliseconds = TimeUnit.MILLISECONDS.convert(firstExpireTime, unit);
            DelayItem<K> delayItem = new DelayItem<>(key, milliseconds, checkTimes);
            _Q.remove(new DelayItem<>(key, 0L, 0));
            _Q.put(delayItem);
        }

        return rs;
    }

    public V get(K key) {
        return _CACHE_MAP.get(key);
    }

    public V remove(K key) {
        V value = _CACHE_MAP.remove(key);

        if (value != null)
            _Q.remove(new DelayItem<>(key, 0L, 0));

        return value;
    }

    public boolean containsKey(K key) {
        return _CACHE_MAP.containsKey(key);
    }

}