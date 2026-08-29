package com.ajaxjs.security.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Represents the bandwidth limit action component.
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.params-sign")
public class BandwidthLimitAction {
    /**
     * Executes the bandwidth limit manager operation.
     *
     * @return the operation result.
     */
    @Bean
    public BandwidthLimitManager bandwidthLimitManager() {
        return new BandwidthLimitManager();
    }
}
