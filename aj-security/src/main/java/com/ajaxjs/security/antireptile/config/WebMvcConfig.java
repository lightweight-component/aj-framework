package com.ajaxjs.security.antireptile.config;

import com.ajaxjs.security.antireptile.AntiReptileInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 将反爬拦截器接入 Spring MVC 的配置。
 * <p>注册路径为所有 MVC 路径；是否需要执行反爬规则由拦截器结合配置与方法注解决定。</p>
 */
//@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 由构造器注入的 MVC 反爬拦截器。
     */
    private final AntiReptileInterceptor antiReptileInterceptor;

    /**
     * 为所有 MVC 路径注册反爬拦截器。
     *
     * @param registry Spring MVC 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(antiReptileInterceptor).addPathPatterns("/**");
    }
}
