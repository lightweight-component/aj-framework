package com.ajaxjs.base.config;

import com.ajaxjs.framework.cache.Cache;
import com.ajaxjs.framework.cache.lfu.LFUCache;
import com.ajaxjs.security.captcha.image.ImageCaptchaConfig;
import com.ajaxjs.security.captcha.image.impl.SimpleCaptchaImage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    static final Cache<String, Object> CACHE = new LFUCache<>(100); // JVM 缓存

    @Bean
    ImageCaptchaConfig ImageCaptchaConfig() {
        ImageCaptchaConfig config = new ImageCaptchaConfig();
        config.setCaptchaImageProvider(new SimpleCaptchaImage());
        config.setSaveToRam(CACHE::put);
        config.setCaptchaCodeFromRam(key -> {
            Object o = CACHE.get(key);
            return o == null ? null : o.toString();
        });
        config.setRemoveByKey(CACHE::remove);

        return config;
    }
}
