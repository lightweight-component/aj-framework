package com.ajaxjs.business.utils.enhancejdkproxy;

/**
 * 抽象的前置方法拦截器类
 * 该类实现了MethodInterceptor接口，并提供了proceed和getJoinPoint方法的具体实现
 * 主要用于在目标方法执行前进行增强处理
 */
public abstract class AbstractBeforeMethodInterceptor implements MethodInterceptor {
    /**
     * 执行增强
     *
     * @param chain         拦截器链，用于调用下一个拦截器或目标方法
     * @param joinPointInfo 连接点信息，包含目标方法的详细信息
     * @return 目标方法的执行结果
     * @throws Exception 如果目标方法执行或增强处理过程中抛出异常
     */
    @Override
    public final Object proceed(InterceptorChain chain, JoinPointInfo joinPointInfo) throws Exception {
        // 调用抽象方法proceed，执行前置增强逻辑
        proceed(joinPointInfo);
        // 调用链的proceed方法，继续执行其他拦截器或目标方法
        return chain.proceed(joinPointInfo);
    }

    /**
     * 获取连接点类型
     *
     * @return 返回连接点类型为BEFORE，表示在目标方法执行前进行拦截和增强
     */
    @Override
    public final JoinPointEnum getJoinPoint() {
        return JoinPointEnum.BEFORE;
    }
}
