package com.ajaxjs.framework.cache.delayqueue.another;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

public class DelayQueueCache implements Runnable {
    private boolean stop = false;

    private final Map<String, String> itemMap = new HashMap<>();

    private final DelayQueue<DelayQueueCacheItem> delayQueue = new DelayQueue<>();

    public DelayQueueCache() {
        // 开启内部线程检测是否过期
        new Thread(this).start();
    }

    /**
     * 添加缓存
     *
     * @param expiryTime 过期时间,单位秒
     */
    public void put(String key, String value, long expiryTime) {
        DelayQueueCacheItem cacheItem = new DelayQueueCacheItem(key, expiryTime);

        delayQueue.add(cacheItem); // 此处忽略添加重复 key 的处理
        itemMap.put(key, value);
    }

    public String get(String key) {
        return itemMap.get(key);
    }

    public void shutdown() {
        stop = true;
    }

    @Override
    public void run() {
        while (!stop) {
            DelayQueueCacheItem cacheItem = delayQueue.poll();

            if (cacheItem != null) {
                // 元素过期, 从缓存中移除
                itemMap.remove(cacheItem.getKey());
                System.out.println("key : " + cacheItem.getKey() + " 过期并移除");
            }
        }

        System.out.println("cache stop");
    }

    public static void main(String[] args) throws InterruptedException {
        // 从执行结果可以看出，因循环内部每次停顿 1 秒，当等待 3 秒后，元素 c 过期并从缓存中清除，等待 4 秒后，元素 b 过期并从缓存中清除，等待 5 秒后，元素 a 过期并从缓存中清除。
        DelayQueueCache cache = new DelayQueueCache();
        // 添加缓存元素
        cache.put("a", "1", 5);
        cache.put("b", "2", 4);
        cache.put("c", "3", 3);

        while (true) {
            String a = cache.get("a");
            String b = cache.get("b");
            String c = cache.get("c");

            System.out.println("a : " + a + ", b : " + b + ", c : " + c);

            // 元素均过期后退出循环
            if (StringUtils.isEmpty(a) && StringUtils.isEmpty(b) && StringUtils.isEmpty(c))
                break;

            TimeUnit.MILLISECONDS.sleep(1000);
        }

        cache.shutdown();
    }
}
