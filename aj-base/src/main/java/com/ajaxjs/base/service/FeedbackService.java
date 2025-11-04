package com.ajaxjs.base.service;

import com.ajaxjs.base.controller.FeedbackController;
import com.ajaxjs.base.model.Feedback;
import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.crud.page.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FeedbackService implements FeedbackController {

    @Override
    public boolean addFeedback(Feedback feedback) {
        return new Action(feedback).create().execute(true).isOk();
    }

    @Override
    public PageResult<Feedback> page() {
        String sql = "SELECT * FROM feedback ORDER BY id DESC";

        return new Action(sql).query().pageByStartLimit(DiContextUtil.getRequest(), Feedback.class);
    }
}
