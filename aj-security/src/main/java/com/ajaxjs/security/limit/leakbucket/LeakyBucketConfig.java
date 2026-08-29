package com.ajaxjs.security.limit.leakbucket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the leaky bucket config component.
 */
@Configuration
public class LeakyBucketConfig {
    /**
     * Executes the leaky bucket operation.
     *
     * @return the operation result.
     */
    @Bean("leakyBucket")
    public LeakyBucket leakyBucket() {
        return new LeakyBucket(10, 5);
    }
}
