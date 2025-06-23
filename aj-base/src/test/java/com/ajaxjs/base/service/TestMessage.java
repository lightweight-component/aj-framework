package com.ajaxjs.base.service;

import com.ajaxjs.model.MailVo;
import com.ajaxjs.service.message.ISendEmail;
import com.ajaxjs.service.tools.IIdCard;
import org.apache.dubbo.config.bootstrap.builders.ReferenceBuilder;
import org.junit.jupiter.api.Test;

public class TestMessage {
    ReferenceBuilder<Object> referenceBuilder = ReferenceBuilder.newBuilder();

    @Test
    public void test() {
        IIdCard demoService = (IIdCard) referenceBuilder.interfaceClass(IIdCard.class)
                .url("tri://localhost:50051")
                .build()
                .get();


        boolean message = demoService.checkIdCard("440105198309060315");
        System.out.println("----------------------------------"+message);
    }

    @Test
    public void testSendEmail() {
        ISendEmail mailService = (ISendEmail) referenceBuilder.interfaceClass(ISendEmail.class)
                .url("tri://localhost:50051")
                .build()
                .get();

        MailVo mail = new MailVo();
        mail.setTo("sp42@qq.com");
        mail.setSubject("hi");
        mail.setContent("test");
        mail.setFrom("frank@ajaxjs.com");

        boolean message = mailService.sendEmail(mail);
        System.out.println(message);
    }
}
