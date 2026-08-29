package com.ajaxjs.framework.validator;

import java.lang.annotation.Annotation;

/**
 * 供应用程序扩展字段和路径变量校验的规则。
 */
public interface ValidatorRule {
    /**
     * 判断当前规则是否支持指定注解。
     *
     * @param annotation 待判断的注解
     * @return 支持时为 {@code true}
     */
    boolean supports(Annotation annotation);

    /**
     * 校验带有此规则支持注解的值。
     *
     * @param annotation 注解实例
     * @param value      待校验的值
     * @param fieldName  字段或路径变量名称
     * @throws ValidatorException 校验失败时抛出
     */
    void validate(Annotation annotation, Object value, String fieldName);
}
