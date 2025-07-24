package com.ajaxjs.business.utils.enhancejdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author xujian
 * 2021-05-26 10:58
 **/
public class EnhanceInvocationHandler implements InvocationHandler {
    //目标对象
    private Object target;
    //拦截器链
    private InterceptorChain chain;

    public EnhanceInvocationHandler(Object target, List<MethodInterceptor> interceptors) {
        this.target = target;
        this.chain = new InterceptorChain(interceptors);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        JoinPointInfo joinPointInfo = JoinPointInfo.Builder.newBuilder()
                .method(method).target(target).proxy(proxy).args(args)
                .build();

        return chain.proceed(joinPointInfo);        //执行方法增强
    }
}
