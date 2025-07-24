package com.ajaxjs.business.utils.enhancejdkproxy;


public class BeforeMethodInterceptor extends AbstractBeforeMethodInterceptor {
    /**
     * 用户自定义增强逻辑
     *
     * @param joinPointInfo
     */
    @Override
    public void proceed(JoinPointInfo joinPointInfo) {
        System.out.println("----before");
    }

    /**
     * 获取顺序编号，可以重写该方法修改默认排序
     *
     * @return 顺序编号
     */
    @Override
    public int getOrder() {
        return 2;
    }
}
