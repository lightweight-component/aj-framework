package org.example.config;

import com.ajaxjs.framework.database.DataBaseConnection;
import com.ajaxjs.iam.client.filter.UserInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

/**
 * 程序配置
 */
@Configuration
public class BaseConfiguration implements WebMvcConfigurer {
    @Value("${db.url}")
    private String url;

    @Value("${db.user}")
    private String user;

    @Value("${db.psw}")
    private String psw;

    @Bean(value = "dataSource", destroyMethod = "close")
    DataSource getDs() {
        return DataBaseConnection.setupMySqlJdbcPool(url, user, psw);
    }


    @Value("${auth.excludes: }")
    private String excludes;

    /**
     * 加入认证拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(authInterceptor());
        interceptorRegistration.addPathPatterns("/**"); // 拦截所有

        // 不需要的拦截路径
        if (StringUtils.hasText(excludes)) {
            String[] arr = excludes.split("\\|");
            interceptorRegistration.excludePathPatterns(arr);
        }
    }

    @Bean
    UserInterceptor authInterceptor() {
        return new UserInterceptor();
    }
}