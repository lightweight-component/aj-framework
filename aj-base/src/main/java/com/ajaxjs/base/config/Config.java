package com.ajaxjs.base.config;

import com.ajaxjs.framework.cache.Cache;
import com.ajaxjs.framework.cache.lfu.LFUCache;
import com.ajaxjs.security.captcha.image.ImageCaptchaConfig;
import com.ajaxjs.security.captcha.image.impl.SimpleCaptchaImage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    static final Cache<String, Object> RAM = new LFUCache<>(100); // JVM 缓存

    @Bean
    ImageCaptchaConfig ImageCaptchaConfig() {
        ImageCaptchaConfig config = new ImageCaptchaConfig();
        config.setCaptchaImageProvider(new SimpleCaptchaImage());
        config.setSaveToRam(RAM::put);
        config.setCaptchaCodeFromRam(key -> RAM.get(key).toString());
        config.setRemoveByKey(RAM::remove);

        return config;
    }
}
