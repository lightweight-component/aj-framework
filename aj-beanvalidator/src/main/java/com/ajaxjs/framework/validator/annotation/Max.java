package com.ajaxjs.framework.validator.annotation;

import java.lang.annotation.*;

/**
 * 约束数值不大于指定上限。
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Max {
    /**
     * 允许的最大值。
     *
     * @return 最大值
     */
    long value();

    /**
     * 校验失败提示语或消息键。
     *
     * @return 提示语或消息键
     */
    String message() default "{max}";
}
