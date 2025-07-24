package com.ajaxjs.framework.profiler;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
public class ProfilerAopTest {
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("application.xml");
        ProfilerBizService profilerBizService = (ProfilerBizService) context.getBean("profilerBizService");
        profilerBizService.A();
    }
}
