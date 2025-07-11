package com.ajaxjs.business.utils.enhancejdkproxy;

/**
 * 抽象的后置返回方法拦截器
 * <p>
 * 该类实现了 MethodInterceptor 接口，并提供了一个模板方法 proceed，用于在目标方法执行后进行拦截和增强
 * 主要用途是在目标方法执行完成后，执行一些额外的逻辑操作，如日志记录、性能统计等
 **/
public abstract class AbstractAfterReturnMethodInterceptor implements MethodInterceptor {
    /**
     * 执行增强
     * <p>
     * 该方法用于在拦截到目标方法调用时，执行增强逻辑它是拦截器链中的关键一环，
     * 通过调用 chain.proceed 方法来继续执行链中的下一个拦截器或目标方法，并通过
     * 自定义的proceed方法添加额外的增强逻辑
     *
     * @param chain         拦截器链对象，用于访问和控制拦截器链的行为
     * @param joinPointInfo 连接点信息对象，包含了关于当前拦截点的详细信息，如方法签名、参数等
     * @return 目标方法执行后的返回值
     * @throws Exception 如果目标方法执行或增强逻辑执行过程中抛出异常，则该方法也会抛出异常
     */
    @Override
    public final Object proceed(InterceptorChain chain, JoinPointInfo joinPointInfo) throws Exception {
        Object o = chain.proceed(joinPointInfo);
        proceed(joinPointInfo);

        return o;
    }

    /**
     * 获取连接点类型
     *
     * @return 连接点类型
     */
    @Override
    public final JoinPointEnum getJoinPoint() {
        return JoinPointEnum.AFTER_RETURN;
    }
}
