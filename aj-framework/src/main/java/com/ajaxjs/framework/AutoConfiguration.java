package com.ajaxjs.framework;

import com.ajaxjs.framework.database.DataBaseConnection;
import com.ajaxjs.framework.mvc.filter.RequestLogger;
import com.ajaxjs.security.traceid.TraceXFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.ajaxjs.framework")
public class AutoConfiguration implements WebMvcConfigurer {
    /**
     * Jackson 映射时更改日期时区
     * 配置同理
     * <pre>{@code
     *  spring:
     *   jackson:
     *     time-zone: GMT+8
     * }</pre>
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        // TODO 能否设置 TimeZone.getTimeZone("Asia/Shanghai")，因为有时 GMT+8 是当时时区+8
        return builder -> builder.timeZone("GMT+8");
    }

    /**
     * 禁用每个请求都自动连接数据库
     */
    @Value("${db.disableAutoConnect:false}")
    private boolean disableAutoConnect;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLogger()).order(0);

        if (!disableAutoConnect)
            registry.addInterceptor(new DataBaseConnection());
    }

//    @Bean
//    public FilterRegistrationBean<TraceXFilter> contentCachingFilter() {
//        FilterRegistrationBean<TraceXFilter> registrationBean = new FilterRegistrationBean<>();
//
//        registrationBean.setFilter(new TraceXFilter());
//        registrationBean.addUrlPatterns("/*"); // 拦截所有路径
//        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE); // 最高优先级
//
//        return registrationBean;
//    }

    @Value("${db.url:}")
    private String url;

    @Value("${db.user:}")
    private String user;

    @Value("${db.psw:}")
    private String psw;

    @Bean(value = "dataSource", destroyMethod = "close")
    @ConditionalOnProperty(name = "db.isDisableAutoConnect", havingValue = "false", matchIfMissing = true)
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
