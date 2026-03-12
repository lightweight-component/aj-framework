package com.ajaxjs.framework.cache;

import com.ajaxjs.framework.cache.lfu.springcache.LFUSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching//开启缓存
@ComponentScan(basePackages = "com.ajaxjs.framework.cache")
public class Config {
    @Bean
    public CacheManager cacheManager() {
        return new LFUSpringCacheManager(100); // LRU 容量为 100，可自行调整
    }
}
