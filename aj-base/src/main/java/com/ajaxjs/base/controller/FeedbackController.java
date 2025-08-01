package com.ajaxjs.base.controller;

import com.ajaxjs.base.model.Feedback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 留言反馈
 */
@RestController
@RequestMapping("/feedback")
public interface FeedbackController {
    /**
     * 新建留言
     *
     * @param feedback 留言
     * @return 是否成功
     */
    @PostMapping
    boolean addFeedback(Feedback feedback);
}
