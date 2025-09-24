package com.ajaxjs.firewall.config;

import com.ajaxjs.firewall.controller.FirewallInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private FirewallInterceptor firewallInterceptor;

    @Value("${firewall.exclude-paths:/actuator/**,/static/**,/css/**,/js/**,/images/**,/favicon.ico}")
    private String excludePathsStr;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 解析排除路径
        List<String> excludePaths = Arrays.asList(excludePathsStr.split(","));

        registry.addInterceptor(firewallInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(excludePaths); // 排除指定路径
    }
}