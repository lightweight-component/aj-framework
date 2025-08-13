package com.ajaxjs.base.service;

import com.ajaxjs.base.controller.FeedbackController;
import com.ajaxjs.base.model.Feedback;
import com.ajaxjs.framework.model.PageVO;
import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.sqlman.Pager;
import com.ajaxjs.sqlman.Sql;
import com.ajaxjs.sqlman.crud.Entity;
import com.ajaxjs.sqlman.model.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FeedbackService implements FeedbackController {

    @Override
    public boolean addFeedback(Feedback feedback) {
        return Entity.instance().input(feedback).create().isOk();
    }

    @Override
    public PageVO<Feedback> page() {
        String sql = "SELECT * FROM feedback ORDER BY id DESC";
        Pager pager = new Pager();
        pager.getParams(DiContextUtil.getRequest());

        PageResult<Feedback> page = Sql.instance().input(sql).page(Feedback.class, pager.getStart(), pager.getLimit());

        return new PageVO<>(page, page.getTotalCount());
    }
}
