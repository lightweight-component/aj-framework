package com.ajaxjs.util.reflect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The reflection utility class for working with fields.
 */
public class Fields {
    /**
     * 获取本类及其父类的字段属性（包括 private 的）
     *
     * @param clz 当前类对象
     * @return 字段数组
     */
    public static Field[] getSuperClassDeclaredFields(Class<?> clz) {
        List<Field> fieldList = new ArrayList<>();

        while (clz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clz.getDeclaredFields())));
            clz = clz.getSuperclass();
        }

        return fieldList.toArray(new Field[0]);
    }

    // TODO 待优化 不用 try...catch

    /**
     * 在指定类及其父类中查找指定名称的字段
     *
     * @param clazz     要查找的类
     * @param fieldName 要查找的字段名称
     * @return 找到的字段对象，如果未找到则返回 null
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 如果当前类没有该字段，则尝试在父类中查找
            Class<?> superClass = clazz.getSuperclass();

            if (superClass != null)
                return findField(superClass, fieldName);
        }

        return null;
    }
}