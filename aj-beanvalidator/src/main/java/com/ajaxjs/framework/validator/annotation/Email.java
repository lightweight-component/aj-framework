package com.ajaxjs.framework.validator.annotation;

import java.lang.annotation.*;

/**
 * 约束字符串为邮箱地址，并可附加正则表达式限制。
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Email {
    /**
     * 额外匹配邮箱地址的正则表达式。
     *
     * @return 正则表达式
     */
    String regexp() default ".*";

    /**
     * 正则表达式匹配标志。
     *
     * @return 匹配标志数组
     */
    Pattern.Flag[] flags() default {};

    /**
     * 校验失败提示语或消息键。
     *
     * @return 提示语或消息键
     */
    String message() default "{email}";
}
