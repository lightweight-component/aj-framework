package com.ajaxjs.security;

import lombok.Data;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.function.BiConsumer;

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
     * Whether this component is enabled
     */
    private boolean isEnabled;

    /**
     * 执行动作
     *
     * @param annotation 注解配置
     * @param req        请求对象
     * @return 拦截器是否通过
     */
    abstract public boolean action(T annotation, HttpServletRequest req);

    public BiConsumer<HttpServletRequest, HttpServletResponse> getAfterCompletionAction() {
        return null;
    }
}
