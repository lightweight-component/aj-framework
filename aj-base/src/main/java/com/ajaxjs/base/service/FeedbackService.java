package com.ajaxjs.base.service;

import com.ajaxjs.base.controller.FeedbackController;
import com.ajaxjs.base.model.Feedback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FeedbackService implements FeedbackController {
    @Override
    public boolean addFeedback(Feedback feedback) {
        log.info("测试反馈info");
        log.debug("测试反馈debug");
        log.error("测试反馈error");
        log.warn("测试反馈warn");

        return false;
    }
}
