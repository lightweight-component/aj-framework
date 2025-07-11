package com.ajaxjs.framework;

import com.ajaxjs.framework.database.DataBaseConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan(basePackages = "com.ajaxjs.framework")
public class AutoConfiguration implements WebMvcConfigurer {
    /**
     * 禁用每个请求都自动连接数据库
     */
    @Value("${db.disableAutoConnect:false}")
    private boolean disableAutoConnect;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (!disableAutoConnect)
            registry.addInterceptor(new DataBaseConnection());
    }
}
