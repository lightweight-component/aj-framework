package com.ajaxjs.message.email;

import com.ajaxjs.message.email.resend.Resend;
import org.junit.jupiter.api.Test;

public class TestSendEmail {
    @Test
    void testResend() {
        new Resend().sendEmail(new Email().setFrom("onboarding@resend.dev").setTo("sp42@qq.com").setSubject("测试邮件").setContent("测试邮件"));
    }
}
