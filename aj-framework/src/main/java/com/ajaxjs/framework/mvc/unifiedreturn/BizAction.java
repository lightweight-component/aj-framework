package com.ajaxjs.framework.mvc.unifiedreturn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为业务操作添加一段可读的、描述性的文字，用于快速理解这次调用的业务意图或上下文
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface BizAction {
    /**
     * message 内容，必填
     *
     * @return message 内容
     */
    String value();
}
