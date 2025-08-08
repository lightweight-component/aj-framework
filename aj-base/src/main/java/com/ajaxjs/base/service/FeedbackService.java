package com.ajaxjs.base.service;

import com.ajaxjs.base.controller.FeedbackController;
import com.ajaxjs.base.model.Feedback;
import com.ajaxjs.sqlman.crud.Entity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FeedbackService implements FeedbackController {
    @Override
    public boolean addFeedback(Feedback feedback) {
        return Entity.instance().input(feedback).create().isOk();
    }
}
