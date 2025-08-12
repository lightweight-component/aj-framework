package com.ajaxjs.base.controller;

import com.ajaxjs.base.model.Feedback;
import com.ajaxjs.security.SecurityInterceptor;
import com.ajaxjs.security.captcha.image.ImageCaptcha;
import com.ajaxjs.security.captcha.image.ImageCaptchaCheck;
import com.ajaxjs.spring.DiContextUtil;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 留言反馈
 */
@RestController
@RequestMapping("/feedback")
public interface FeedbackController {
    @GetMapping("/captcha")
    default void showCaptcha(HttpServletRequest req, HttpServletResponse response) {
        ImageCaptcha imageCaptcha = DiContextUtil.getBean(ImageCaptcha.class);
        imageCaptcha.captchaImage(req, response);
    }

    /**
     * 新建留言
     *
     * @param feedback 留言
     * @return 是否成功
     */
    @PostMapping
    @CrossOrigin
    @ImageCaptchaCheck
    boolean addFeedback(@RequestBody Feedback feedback);
}
