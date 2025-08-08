package com.ajaxjs.base.controller;

import com.ajaxjs.base.model.Feedback;
import org.springframework.web.bind.annotation.*;

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
    @CrossOrigin
    boolean addFeedback(@RequestBody Feedback feedback);
}
