package com.ajaxjs.framework.validator.annotation;

import java.lang.annotation.*;

/**
 * 约束字符序列非 {@code null} 且至少包含一个非空白字符。
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlank {
    /**
     * 校验失败提示语或消息键。
     *
     * @return 提示语或消息键
     */
    String message() default "{not-blank}";
}
