package com.ajaxjs.service.message;

import com.ajaxjs.model.MailVo;
import com.ajaxjs.service.IService;

public interface ISendEmail extends IService {
    /**
     * 发送邮件
     *
     * @param mail 邮件
     * @return 是否成功
     */
    boolean sendEmail(MailVo mail);
}
