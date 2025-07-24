package com.ajaxjs.business.utils.enhancejdkproxy;

public class AroundMethodInterceptor implements MethodInterceptor {
    /**
     * 执行增强
     *
     * @param chain
     * @param joinPointInfo
     * @return
     * @throws Exception
     */
    @Override
    public Object proceed(InterceptorChain chain, JoinPointInfo joinPointInfo) throws Exception {
        System.out.println("----around-before");
        Object o = chain.proceed(joinPointInfo);
        System.out.println("----around-after");

        return o;
    }

    /**
     * 用户自定义增强逻辑
     *
     * @param joinPointInfo
     */
    @Override
    public void proceed(JoinPointInfo joinPointInfo) throws Exception {

    }

    /**
     * 获取连接点类型
     *
     * @return 连接点类型
     */
    @Override
    public JoinPointEnum getJoinPoint() {
        return JoinPointEnum.AROUND;
    }
}
