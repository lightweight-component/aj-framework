package com.ajaxjs.base;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedissonAutoConfiguration.class})
@EnableDubbo
public class BaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(BaseApplication.class, args);
    }
}
