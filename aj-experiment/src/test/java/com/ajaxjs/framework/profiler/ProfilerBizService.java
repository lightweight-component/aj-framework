package com.ajaxjs.framework.profiler;

public class ProfilerBizService {
    public void A() throws Exception {
        Thread.sleep(200);
        A1();
        System.out.println("iamzhongyong");
    }

    public void A1() throws Exception {
        Thread.sleep(200);
        A11();
    }

    public void A11() throws Exception {
        Thread.sleep(200);
    }

    public void A2() throws Exception {
        Thread.sleep(200);
        A1();
    }
}
