package com.ajaxjs.framework.confighotreload;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Data
@Component
@ConditionalOnProperty(name = "aj-framework.confighotreload.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "app") // 绑定配置前缀
public class AppConfig {
    private int timeout = 3000; // 默认超时时间3秒
    private int maxRetries = 2; // 默认重试次数2次

    @Bean
    public ConfigFileWatcher configFileWatcher(ConfigRefreshHandler refreshHandler) {
        return new ConfigFileWatcher(refreshHandler);
    }
}