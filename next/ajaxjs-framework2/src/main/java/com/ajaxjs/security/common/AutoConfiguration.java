package com.ajaxjs.security.common;

import com.ajaxjs.security.SecurityInterceptor;
import com.ajaxjs.security.paramssign.ParamsSignLocal;
import com.ajaxjs.security.paramssign.ParamsSignResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@ComponentScan(basePackages = "com.ajaxjs.security")
public class AutoConfiguration implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(initSecurityInterceptor());
    }

    @Bean
    HandlerInterceptor initSecurityInterceptor() {
        return new SecurityInterceptor();
    }

    @Value("${security.ParamsSign.enabled:false}")
    private boolean paramsSignEnabled;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        if (paramsSignEnabled)
            resolvers.add(new ParamsSignResolver(paramsSignLocal()));
    }

    @Bean
    @ConditionalOnProperty(name = "security.ParamsSign.enabled", havingValue = "true")
    ParamsSignLocal paramsSignLocal() {
        return new ParamsSignLocal();
    }
}
