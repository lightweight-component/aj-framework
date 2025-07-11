package com.ajaxjs.spring.mvc.unifiedreturn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 统一返回对象自定义 message 内容
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JsonMessage {
    /**
     * message 内容，必填
     *
     * @return message 内容
     */
    String value();
}
