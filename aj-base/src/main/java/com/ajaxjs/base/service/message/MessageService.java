package com.ajaxjs.base.service.message;


import com.ajaxjs.base.service.message.email.Mail;
import com.ajaxjs.base.service.message.email.Sender;
import com.ajaxjs.model.MailVo;
import com.ajaxjs.service.message.ISendEmail;
import com.ajaxjs.service.message.ISendSms;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@DubboService
public class MessageService implements ISendEmail, ISendSms {
    @Autowired(required = false)
    Mail mailCfg;

    @Override
    public boolean sendEmail(MailVo mail) {
        System.out.println(mailCfg);
        BeanUtils.copyProperties(mail, mailCfg);

        return Sender.send(mailCfg);
    }

    @Override
    public boolean send(String phone, String code) {
        return false;
    }
}
