package org.example.service;

import feign.Logger;
import org.springframework.context.annotation.Bean;

public class ClientConfiguration {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
