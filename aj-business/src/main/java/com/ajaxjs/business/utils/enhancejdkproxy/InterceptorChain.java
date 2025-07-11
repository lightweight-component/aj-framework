package com.ajaxjs.business.utils.enhancejdkproxy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * 拦截器链
 **/
public class InterceptorChain {
    /**
     * 拦截器列表
     */
    private final List<MethodInterceptor> methodInterceptors = new ArrayList<>();

    private int currentInterceptorIndex = -1;

    public InterceptorChain(List<MethodInterceptor> interceptors) {
        //根据增强时机（连接点）分组
        Map<String, List<MethodInterceptor>> interceptorMap = interceptors.stream().collect(groupingBy(m -> m.getJoinPoint().getName()));
        //方法执行前增强的拦截器
        List<MethodInterceptor> beforeInterceptors = interceptorMap.get(JoinPointEnum.BEFORE.getName());
        //按照order排序
        beforeInterceptors.sort(Comparator.comparingInt(MethodInterceptor::getOrder));
        //方法返回后增强的拦截器
        List<MethodInterceptor> afterReturnInterceptors = interceptorMap.get(JoinPointEnum.AFTER_RETURN.getName());
        //按照order排序
        afterReturnInterceptors.sort(Comparator.comparingInt(MethodInterceptor::getOrder));
        //方法环绕增强的拦截器
        List<MethodInterceptor> aroundInterceptors = interceptorMap.get(JoinPointEnum.AROUND.getName());
        //按照order排序
        aroundInterceptors.sort(Comparator.comparingInt(MethodInterceptor::getOrder));
        //按照around->before->afterReturn的顺序添加到methodInterceptors
        methodInterceptors.addAll(aroundInterceptors);
        methodInterceptors.addAll(beforeInterceptors);
        methodInterceptors.addAll(afterReturnInterceptors);
    }

    /**
     * 执行方法
     *
     * @param joinPointInfo
     * @return
     * @throws Exception
     */
    public Object proceed(JoinPointInfo joinPointInfo) throws Exception {
        if (currentInterceptorIndex == methodInterceptors.size() - 1)
            //执行目标方法
            return joinPointInfo.getMethod().invoke(joinPointInfo.getTarget(), joinPointInfo.getArgs());

        MethodInterceptor methodInterceptor = methodInterceptors.get(++currentInterceptorIndex);

        return methodInterceptor.proceed(this, joinPointInfo);
    }
}
