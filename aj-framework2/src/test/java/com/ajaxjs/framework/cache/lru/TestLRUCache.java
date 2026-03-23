
package com.ajaxjs.framework.cache.lru;

import org.junit.jupiter.api.Test;

public class TestLRUCache {
    @Test
    void test() throws InterruptedException {
        LRUCache<String, String> cache = new LRUCache<>(3, 2000); // 最多3个，TTL 2秒

        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");

        System.out.println(cache.get("a")); // 1

        Thread.sleep(2500);

        System.out.println(cache.get("a")); // null（已过期）
        cache.put("d", "4"); // 触发 LRU 淘汰（可能是 b 或 c，取决于访问）

        System.out.println(cache.keySet()); // 只剩未过期的
    }
}
