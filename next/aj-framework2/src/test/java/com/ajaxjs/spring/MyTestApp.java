package com.ajaxjs.spring;

import com.ajaxjs.spring.traceid.TracedThreadPoolTaskExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class MyTestApp {
    public static void main(String[] args) {
        SpringApplication.run(MyTestApp.class, args);
    }

    @Bean("MyExecutor")
    public Executor asyncExecutor() {
        TracedThreadPoolTaskExecutor executor = new TracedThreadPoolTaskExecutor();
        return executor;
    }
}
