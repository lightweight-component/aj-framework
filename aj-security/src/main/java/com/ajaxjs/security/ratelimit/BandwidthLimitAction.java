package com.ajaxjs.security.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Data
@Component
//@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "security.params-sign")
public class BandwidthLimitAction {
    @Bean
    public BandwidthLimitManager bandwidthLimitManager() {
        return new BandwidthLimitManager();
    }
}
