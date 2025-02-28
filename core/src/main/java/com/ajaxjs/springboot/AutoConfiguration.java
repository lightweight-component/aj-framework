package com.ajaxjs.springboot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.ajaxjs.springboot")
public class AutoConfiguration {
    /**
     * Spring 程序启动的时间
     */
    public static final long APP_START_TIME = System.currentTimeMillis();

    /**
     * 全局异常拦截器
     *
     * @return 全局异常拦截器
     */
    @Bean
    public GlobalExceptionHandler GlobalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * Spring IoC 工具
     *
     * @return IoC 工具
     */
    @Bean
    public DiContextUtil DiContextUtil() {
        return new DiContextUtil();
    }
}
