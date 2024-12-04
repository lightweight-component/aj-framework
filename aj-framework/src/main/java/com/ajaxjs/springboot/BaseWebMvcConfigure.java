package com.ajaxjs.springboot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class BaseWebMvcConfigure implements WebMvcConfigurer {
    /**
     * Spring 程序启动的时间
     */
    public static final long APP_START_TIME = System.currentTimeMillis();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new GlobalControllerInterceptor());
    }

}
