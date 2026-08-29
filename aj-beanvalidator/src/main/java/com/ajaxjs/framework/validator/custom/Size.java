package com.ajaxjs.framework.validator.custom;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 约束字符序列、集合、映射或数组的长度范围。
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Size {
    /**
     * 允许的最小长度。
     *
     * @return 最小长度
     */
    int min() default 0;

    /**
     * 允许的最大长度。
     *
     * @return 最大长度
     */
    int max() default Integer.MAX_VALUE;

    /**
     * 校验失败提示语或消息键。
     *
     * @return 提示语或消息键
     */
    String message() default "{size}";
}
