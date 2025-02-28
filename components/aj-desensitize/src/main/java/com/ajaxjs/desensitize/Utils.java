package com.ajaxjs.desensitize;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

/**
 * bean相互转换工具类
 */
class Utils {
    public static Object getValue(Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal Access when getting the Value from " + field + " on object of " + obj, e);
        }
    }

    public static void setField(Field field, Object obj, Object value) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal Access when setting the Field of " + field + " on object of " + obj + "to the value is " + value, e);
        }
    }

    /**
     * 获取指定类及其所有父类中带有指定名称的字段。
     *
     * @param clazz       要从中查找字段的类
     * @param name        字段的名称
     * @param forceAccess 是否强制访问私有或受保护的字段
     * @return 包含指定名称的字段
     * @throws IllegalArgumentException 如果找不到具有指定名称的字段，或者类或字段名是 null
     */
    public static Field getField(Class<?> clazz, String name, boolean forceAccess) {
        if (clazz == null || name == null || name.isEmpty())
            throw new IllegalArgumentException("Class and field name must not be null or empty");

        Class<?> currentClass = clazz;

        while (currentClass != null) {
            try {
                // 尝试在当前类中获取字段
                Field field = currentClass.getDeclaredField(name);
                if (forceAccess)
                    field.setAccessible(true);

                return field;
            } catch (NoSuchFieldException ignored) {
                // 如果当前类没有该字段，则继续查找父类
            }

            // 移动到父类
            currentClass = currentClass.getSuperclass();
        }

        // 如果到达 Object 类还没有找到字段，则抛出异常
        throw new IllegalArgumentException("Cannot find field named '" + name + "' in class hierarchy of " + clazz.getName());
    }

    public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getAllFields(clazz, field -> field.isAnnotationPresent(annotation));
    }

    public static List<Field> getAllFields(Class<?> clazz) {
        return getAllFields(clazz, null);
    }

    /**
     * 获取指定类及其所有父类和接口中声明的所有字段。
     *
     * @param clazz 要获取字段的类
     * @return 包含所有字段的列表
     */
    public static List<Field> getAllFields(Class<?> clazz, Predicate<Field> fn) {
        if (clazz == null)
            throw new IllegalArgumentException("Class must not be null");

        Set<String> fieldNames = new HashSet<>();
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null) {
            // 获取当前类的所有字段
            for (Field field : currentClass.getDeclaredFields()) {
                if (fieldNames.add(field.getName())) {
                    if (fn == null || fn.test(field))
                        fields.add(field);
                }
            }

            // 遍历当前类实现的接口
            for (Class<?> interF : currentClass.getInterfaces())
                fields.addAll(getAllFields(interF, fn));

            // 移动到父类
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * 判断是否是无需解析的值对象
     *
     * @param value 值对象
     * @return 是-true 否-false
     */
    public static boolean isFinal(Object value) {
        if (Objects.isNull(value)) {
            return true;
        } else if (value instanceof String) {
            return true;
        } else if (value instanceof Integer) {
            return true;
        } else if (value instanceof Short) {
            return true;
        } else if (value instanceof Long) {
            return true;
        } else if (value instanceof Double) {
            return true;
        } else if (value instanceof Float) {
            return true;
        } else if (value instanceof Byte) {
            return true;
        } else if (value instanceof Boolean) {
            return true;
        } else if (value instanceof Character) {
            return true;
        } else if (value instanceof Number) {
            return true;
        } else return value.getClass().isEnum();
    }

    /**
     * 指定的修饰符是否序列化
     *
     * @param field 字段反射类型
     * @return true -是 false-否
     */
    public static boolean isModifierFinal(final Field field) {
        int modifiers = field.getModifiers();

        return checkModifierFinalStaticTransVol(modifiers) || checkModifierNativeSyncStrict(modifiers);
    }

    protected static boolean checkModifierNativeSyncStrict(int modifiers) {
        return Modifier.isNative(modifiers) || Modifier.isSynchronized(modifiers) || Modifier.isStrict(modifiers);
    }

    protected static boolean checkModifierFinalStaticTransVol(int modifiers) {
        return Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || Modifier.isVolatile(modifiers);
    }

    /**
     * Removes the element at the specified position in the specified array.
     * Shifts any subsequent elements to the left (subtracts one from their indices).
     *
     * @param <T>   the component type of the array
     * @param array the array to remove the element from, may be {@code null}
     * @param index the position of the element to be removed
     * @return A new array containing the existing elements except the removed element,
     * or {@code null} if the input array is {@code null} or index is invalid.
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] remove(T[] array, int index) {
        if (array == null || index < 0 || index >= array.length)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + (array != null ? array.length : 0));

        // Create a new array with one less element
        T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);

        // Copy elements to the new array, skipping the element at the specified index
        for (int i = 0, j = 0; i < array.length; i++) {
            if (i != index)
                newArray[j++] = array[i];
        }

        return newArray;
    }
}
