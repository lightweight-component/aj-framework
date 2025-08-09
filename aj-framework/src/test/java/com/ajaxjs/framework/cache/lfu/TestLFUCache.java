package com.ajaxjs.framework.cache.lfu;

import org.junit.jupiter.api.Test;

public class TestLFUCache {
    @Test
    void test() throws InterruptedException {
        //        LFUCacheWithPerEntryTTL<String, String> cache = new LFUCacheWithPerEntryTTL<>(3);
//
//        cache.put("A", "Apple", 2);     // 2秒后过期
//        cache.put("B", "Banana", 5);    // 5秒后过期
//        cache.put("C", "Cherry");       // 永不过期（使用默认或无 TTL）
//
//        System.out.println(cache.get("A")); // Apple
//        Thread.sleep(3000);
//        System.out.println(cache.get("A")); // null（已过期）
//
//        System.out.println(cache.get("B")); // Banana
//        System.out.println(cache.get("C")); // Cherry

        // 启用后台清理，每 500ms 扫描一次
        LFUCache<String, String> cache2 = new LFUCache<>(100, 500, true);

        cache2.put("A", "Apple", 2);   // 2秒后过期
        cache2.put("B", "Banana", 5);  // 5秒后过期

        System.out.println("1s后: " + cache2.get("A")); // Apple
        Thread.sleep(3000);
        System.out.println("3s后: " + cache2.get("A")); // null（后台或get时已清理）

        // 关闭缓存，释放线程
        cache2.close();
    }
}
