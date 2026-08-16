package com.ajaxjs.message.model.scheme;

import java.lang.annotation.*;

/**
 * 多对象输入字段
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiObjField {
    /**
     * 字段名称
     */
    String value() default "";

    /**
     * 字段描述
     */
    String description() default "";
}
