package com.ajaxjs.api;

import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;

/**
 * 拦截器动作
 *
 * @param <T>
 */
@Data
public abstract class InterceptorAction<T extends Annotation> {
    /**
     * Whether is global access check
     */
    private boolean isGlobalCheck;

    /**
     * 执行动作
     *
     * @param annotation 注解配置
     * @param req        请求对象
     * @return 拦截器是否通过
     */
    abstract public boolean action(T annotation, HttpServletRequest req);
}
