package com.ajaxjs.security;

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

/**
 * Represents the security configuration component.
 */
@Configuration
@ComponentScan(basePackages = "com.ajaxjs.security")
public class SecurityConfiguration implements WebMvcConfigurer {
    /**
     * Executes the add interceptors operation.
     *
     * @param registry the registry parameter.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(initSecurityInterceptor());
    }

    @Bean
    HandlerInterceptor initSecurityInterceptor() {
        return new SecurityInterceptor();
    }

    /**
     * Stores the params sign enabled value.
     */
    @Value("${security.ParamsSign.enabled:false}")
    private boolean paramsSignEnabled;

    /**
     * Executes the add argument resolvers operation.
     *
     * @param resolvers the resolvers parameter.
     */
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
