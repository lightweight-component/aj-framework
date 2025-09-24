package com.ajaxjs.redis.leveltwocache;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


//@TestPropertySource(locations = "classpath:application.yaml")
public class TestLevelTwoCache {
    //    @Resource
    LevelTwoCacheManager cacheManager;

    String _value = "h888";

    @Test
    void test() {
        cacheManager.createRedisCache().put("foo2", _value);
        Object value;
        value = cacheManager.getCache("").get("foo2").get(); // First time L1 is not ready, get from Redis(L2)
        System.out.println(value);
        assertEquals(_value, value);

        value = cacheManager.getCache("").get("foo2").get(); // Second time L1 is ready, get from L1
        System.out.println(value);
        assertEquals(_value, value);


        cacheManager.put("foo3", _value);
        String v2 = cacheManager.get("foo3", String.class);
        System.out.println(v2);
        assertEquals(_value, v2);

        v2 = cacheManager.getString("foo3");
        System.out.println(v2);
        assertEquals(_value, v2);
    }

}

