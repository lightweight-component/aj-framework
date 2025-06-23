package com.ajaxjs.oauth.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class AuthStateCacheTest {

    @Test
    public void cache1() throws InterruptedException {
        AuthDefaultStateCache.INSTANCE.cache("key", "value");

        Assertions.assertEquals(AuthDefaultStateCache.INSTANCE.get("key"), "value");

        TimeUnit.MILLISECONDS.sleep(4);
        Assertions.assertEquals(AuthDefaultStateCache.INSTANCE.get("key"), "value");
    }

    @Test
    public void cache2() throws InterruptedException {
        AuthDefaultStateCache.INSTANCE.cache("key", "value", 10);
        Assertions.assertEquals(AuthDefaultStateCache.INSTANCE.get("key"), "value");

        // 没过期
        TimeUnit.MILLISECONDS.sleep(5);
        Assertions.assertEquals(AuthDefaultStateCache.INSTANCE.get("key"), "value");

        // 过期
        TimeUnit.MILLISECONDS.sleep(6);
        Assertions.assertNull(AuthDefaultStateCache.INSTANCE.get("key"));
    }
}
