package com.ajaxjs.framework.validator.custom;

import java.lang.annotation.*;

/**
 * 约束值不为 {@code null}。
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotNull {
    /**
     * 校验失败提示语或消息键。
     *
     * @return 提示语或消息键
     */
    String message() default "{not-null}";
}
