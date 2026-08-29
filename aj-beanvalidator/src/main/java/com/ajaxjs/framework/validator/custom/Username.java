package com.ajaxjs.framework.validator.custom;

import java.lang.annotation.*;

/**
 * 约束字符串符合内置用户名格式。
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Username {
    /**
     * 校验失败提示语或消息键。
     *
     * @return 提示语或消息键
     */
    String message() default "{username}";

    /**
     * 指定字段是否必须提供值。
     *
     * @return 必填时为 {@code true}
     */
    boolean required() default true;
}
