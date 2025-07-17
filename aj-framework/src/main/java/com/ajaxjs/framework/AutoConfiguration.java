package com.ajaxjs.framework;

import com.ajaxjs.framework.database.DataBaseConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

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

    @Value("${db.url:}")
    private String url;

    @Value("${db.user:}")
    private String user;

    @Value("${db.psw:}")
    private String psw;

    @Bean(value = "dataSource", destroyMethod = "close")
    DataSource getDs() {
        return DataBaseConnection.setupMySqlJdbcPool(url, user, psw);
    }

    @Value("${aj-framework.allowCrossDomain:false}")
    private boolean allowCrossDomain;

    /**
     * 跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowCrossDomain)
            registry.addMapping("/**").allowedHeaders("*").allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE").allowedOrigins("*");
    }
}
