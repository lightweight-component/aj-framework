package com.ajaxjs.message.model.config;

import com.ajaxjs.message.model.enumration.ConfigValueType;

import java.lang.annotation.*;

/**
 * 标记字段为配置值
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigValue {
    /**
     * 字段名称
     */
    String value() default "";

    /**
     * 字段描述
     */
    String description() default "";

    /**
     * 默认取对应的 Java 类型
     */
    ConfigValueType type() default ConfigValueType.AUTO;
}
