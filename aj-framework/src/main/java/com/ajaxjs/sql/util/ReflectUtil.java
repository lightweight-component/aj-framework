package com.ajaxjs.sql.util;


import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtil {
    /**
     * 根据类、方法的字符串和参数列表获取方法对象，支持重载的方法
     *
     * @param obj    可以是实例对象，也可以是类对象
     * @param method 方法名称
     * @param args   明确的参数类型列表
     * @return 匹配的方法对象，null 表示找不到
     */
    public static Method getMethod(Object obj, String method, Class<?>... args) {
        Class<?> cls = obj instanceof Class ? (Class<?>) obj : obj.getClass();

        try {
            return ObjectUtils.isEmpty(args) ? cls.getMethod(method) : cls.getMethod(method, args);
        } catch (NoSuchMethodException | SecurityException e) {
            System.err.println("类找不到这个方法 " + method);
            return null;
        }
    }

    /**
     * 把参数转换为类对象列表
     * 这个 Java 函数将一个可变参数列表转换为一个类对象列表。它接受一个可变参数 args，返回一个 Class 类型的数组 clazz，
     * 数组长度与参数列表的长度相同，并且每个元素的类型与对应参数的类型相同。
     *
     * @param args 可变参数列表
     * @return 类对象列表
     */
    public static Class<?>[] args2class(Object[] args) {
        Class<?>[] clazz = new Class[args.length];

        for (int i = 0; i < args.length; i++)
            clazz[i] = args[i].getClass();

        return clazz;
    }

    /**
     * 调用方法
     *
     * @param instance 对象实例，bean
     * @param method   方法对象名称
     * @param args     参数列表
     * @return 执行结果
     */
    public static Object executeMethod(Object instance, String method, Object... args) {
        // 没有方法对象，先找到方法对象。可以支持方法重载，按照参数列表
        Class<?>[] clazz = args2class(args);
        Method methodObj = getMethod(instance.getClass(), method, clazz);

        return methodObj != null ? executeMethod(instance, methodObj, args) : null;
    }

    /**
     * 调用方法，该方法不会抛出异常
     *
     * @param instance 对象实例，bean
     * @param method   方法对象
     * @param args     参数列表
     * @return 执行结果
     */
    public static Object executeMethod(Object instance, Method method, Object... args) {
        try {
            return executeMethod_Throwable(instance, method, args);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 调用方法
     *
     * @param instance 对象实例，bean
     * @param method   方法对象
     * @param args     参数列表
     * @return 执行结果
     * @throws Throwable 任何异常
     */
    public static Object executeMethod_Throwable(Object instance, Method method, Object... args) throws Throwable {
        if (instance == null || method == null)
            return null;

        try {
            return args == null || args.length == 0 ? method.invoke(instance) : method.invoke(instance, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
            Throwable e;

            if (e1 instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e1).getTargetException();
                throw e;
            }

            throw e1;
        }
    }

}
