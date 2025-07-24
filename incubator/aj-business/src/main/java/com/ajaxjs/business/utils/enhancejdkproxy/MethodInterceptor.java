package com.ajaxjs.business.utils.enhancejdkproxy;


public interface MethodInterceptor {
    /**
     * 执行增强
     *
     * @param chain
     * @param joinPointInfo
     * @return
     * @throws Exception
     */
    Object proceed(InterceptorChain chain, JoinPointInfo joinPointInfo) throws Exception;

    /**
     * 用户自定义增强逻辑
     *
     * @param joinPointInfo
     */
    void proceed(JoinPointInfo joinPointInfo) throws Exception;

    /**
     * 获取连接点类型
     *
     * @return 连接点类型
     */
    JoinPointEnum getJoinPoint();

    /**
     * 获取顺序编号，可以重写该方法修改默认排序，越小越先执行
     *
     * @return
     */
    default int getOrder() {
        return 1;
    }
}
