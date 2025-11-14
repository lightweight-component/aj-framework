package com.ajaxjs.framework.service;


import com.ajaxjs.framework.BaseTest;
import com.ajaxjs.framework.business.model.Feedback;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFeedbackService extends BaseTest {
//    @Autowired
//    FeedbackService feedbackService;

    @Test
    void testAddFeedback() {
        Feedback feedback = new Feedback();
        feedback.setName("测试");
        feedback.setEmail("test@gmail.com");
        feedback.setPhone("12345678901");
        feedback.setFeedback("测试反馈");
        feedback.setContact("测试联系人");
        feedback.setContent("测试内容");

//        boolean result = feedbackService.addFeedback(feedback);
//        assertTrue(result);
    }
}