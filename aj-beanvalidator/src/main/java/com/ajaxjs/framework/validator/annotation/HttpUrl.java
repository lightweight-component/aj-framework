package com.ajaxjs.framework.validator.annotation;

import java.lang.annotation.*;

/**
 * 约束字符串为 HTTP 或 HTTPS URL。
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpUrl {
    /**
     * 校验失败提示语或消息键。
     *
     * @return 提示语或消息键
     */
    String message() default "{http-url}";

    /**
     * 指定字段是否必须提供值。
     *
     * @return 必填时为 {@code true}
     */
    boolean required() default true;
}
