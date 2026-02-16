package com.ajaxjs.base.controller;

import com.ajaxjs.base.model.Feedback;
import com.ajaxjs.security.captcha.image.ImageCaptcha;
import com.ajaxjs.security.captcha.image.ImageCaptchaCheck;
import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.sqlman.crud.page.PageResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 留言反馈
 */
@RestController
@RequestMapping("/feedback")
public interface FeedbackController {
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

    @GetMapping("/captcha")
    default void showCaptcha(HttpServletRequest req, HttpServletResponse response) {
        ImageCaptcha imageCaptcha = DiContextUtil.getBean(ImageCaptcha.class);
        imageCaptcha.captchaImage(req, response);
    }

    @GetMapping
    public ResponseResultWrapper test() {
        return new ResponseResultWrapper().setStatus(1).setData("Welcome to AJ-base!");
    }
}
