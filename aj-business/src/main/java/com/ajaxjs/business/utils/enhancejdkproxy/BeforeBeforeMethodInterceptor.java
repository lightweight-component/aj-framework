package com.ajaxjs.business.utils.enhancejdkproxy;


public class BeforeBeforeMethodInterceptor extends AbstractBeforeMethodInterceptor {
    /**
     * 用户自定义增强逻辑
     *
     * @param joinPointInfo
     */
    @Override
    public void proceed(JoinPointInfo joinPointInfo) {
        System.out.println("----beforebefore");
    }
}
