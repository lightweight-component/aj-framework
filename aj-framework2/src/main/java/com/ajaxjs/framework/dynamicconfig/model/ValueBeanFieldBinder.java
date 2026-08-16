package com.ajaxjs.framework.dynamicconfig.model;

import lombok.Data;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * Binding metadata that describes how a bean field should be refreshed when configuration changes.
 */
@Data
public class ValueBeanFieldBinder {
    /**
     * Value placeholder / Value SpringEL Expression / ConfigurationProperties annotation prefix
     */
    private String expr;

    /**
     * Reference of the Dynamic Bean instance
     */
    private WeakReference<Object> beanRef;

    /**
     * Record the bound field, only for {@literal @}Value fields binding case
     */
    private Field dynamicField;

    /**
     * name of the Spring bean
     */
    private String beanName;

    /**
     * Create a binder for a {@code @Value} field or a {@code @ConfigurationProperties} bean.
     *
     * @param expr         the original {@code @Value} expression or configuration prefix
     * @param dynamicField the bound field for {@code @Value} binding, or {@code null} for {@code @ConfigurationProperties}
     * @param bean         the bean instance
     * @param beanName     Spring bean name
     */
    public  ValueBeanFieldBinder(String expr, Field dynamicField, Object bean, String beanName) {
        this.beanRef = new WeakReference<>(bean);
        this.expr = expr;
        this.dynamicField = dynamicField;
        this.beanName = beanName;
    }
}
