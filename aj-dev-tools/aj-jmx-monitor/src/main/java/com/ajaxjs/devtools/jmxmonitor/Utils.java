package com.ajaxjs.devtools.jmxmonitor;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class Utils {
    /**
     * 设置 Java Bean 的值
     *
     * @param bean      Bean 实体
     * @param fieldName 字段名
     * @param value     值
     */
    public static void setBeanValue(Object bean, String fieldName, Object value) {
        try {
            Field field = bean.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            // the exception here doesn't need to handle
            log.warn("访问字段时候 {} 失败", fieldName);
        } catch (NoSuchFieldException e) {
            log.warn("No Such Field: {}", fieldName);
        }
    }
}
