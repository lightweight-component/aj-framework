package com.ajaxjs.message.model.scheme;

import com.ajaxjs.message.model.enumration.SchemeValueType;

import java.lang.annotation.*;

/**
 * 标记字段为配置值
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SchemeValue {

    /**
     * 字段名称
     */
    String value() default "";

    /**
     * 字段描述
     */
    String description() default "";

    /**
     * 默认取对应的java类型
     */
    SchemeValueType type() default SchemeValueType.AUTO;

    /**
     * 如果是选项型，这里可以直接指定有哪些选项
     */
    SchemeValueOption[] options() default {};

}
