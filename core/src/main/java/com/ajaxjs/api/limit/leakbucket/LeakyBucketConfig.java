package com.ajaxjs.api.limit.leakbucket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LeakyBucketConfig {
    @Bean("leakyBucket")
    public LeakyBucket leakyBucket() {
        return new LeakyBucket(10, 5);
    }
}
