package com.ajaxjs.framework.validator.annotation;

import java.lang.annotation.*;

/**
 * 约束字符串为中国居民身份证号码。
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IdCard {
    /**
     * 校验失败提示语或消息键。
     *
     * @return 提示语或消息键
     */
    String message() default "{id-card}";

    /**
     * 指定字段是否必须提供值。
     *
     * @return 必填时为 {@code true}
     */
    boolean required() default true;
}
