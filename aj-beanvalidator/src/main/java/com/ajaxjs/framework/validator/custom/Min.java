package com.ajaxjs.framework.validator.custom;

import java.lang.annotation.*;

/**
 * 约束数值不小于指定下限。
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Min {
    /**
     * 允许的最小值。
     *
     * @return 最小值
     */
    long value();

    /**
     * 校验失败提示语或消息键。
     *
     * @return 提示语或消息键
     */
    String message() default "{min}";
}
