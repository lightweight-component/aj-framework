package com.ajaxjs.util.reflect;

import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The reflection utility class for creating new instances of classes.
 */
@Slf4j
public class NewInstance<T> {
    private final Class<T> clz;

    private Object[] args;

    /**
     * 创建实例
     *
     * @param clz 要创建实例的类对象
     */
    public NewInstance(Class<T> clz) {
        this.clz = clz;
    }

    /**
     * 创建实例
     *
     * @param clz  要创建实例的类对象
     * @param args 构造函数参数列表
     */
    public NewInstance(Class<T> clz, Object... args) {
        this(clz);
        this.args = args;
    }

    /**
     * 创建实例
     * 该函数根据给定的参数列表创建指定类的实例。如果参数列表为空，则使用默认构造函数创建实例；否则，使用指定的构造函数创建实例。
     *
     * @return 创建的实例对象
     */
    public T newInstance() {
        if (ObjectHelper.isEmpty(args))
            try {
                return clz.newInstance();
            } catch (Throwable e) {
                log.error("The constructor of this class " + clz.getName() + " is not found.", e);
                throw new RuntimeException("The constructor of this class " + clz.getName() + " is not found.", e);
            }
        else {
            Constructor<T> constructor = getConstructor(clz, args2class(args));// 获取构造器

            return newInstance(constructor, args);
        }
    }

    /**
     * 根据构造器创建实例
     * 该函数根据给定的构造器和参数列表创建指定类的实例。它使用反射调用构造函数来实例化对象，并在实例化失败时返回 null。
     *
     * @param constructor 类构造器
     * @param args        获取指定参数类型的构造函数，这里传入我们想调用的构造函数所需的参数。可以不传。
     * @param <T>         类引用
     * @return 对象实例
     */
    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            log.error("Error occurred when creating instance of class: " + constructor.getDeclaringClass(), e);
            throw new RuntimeException("Error occurred when creating instance of class: " + constructor.getDeclaringClass(), e);
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
     * 获取类的构造器，可以支持重载的构造器（不同参数的构造器）
     * 这个函数用于获取类的构造函数。它接受两个参数，一个是类对象，一个是可选的参数类型数组。
     * 如果传入了参数类型数组，则获取与该数组匹配的构造函数；如果没有传入参数类型数组，则获取空参数列表的构造函数。
     * 如果找不到合适的构造函数，会记录日志并返回 null。
     *
     * @param clz    类对象
     * @param argClz 指定构造函数的参数类型，这里传入我们想调用的构造函数所需的参数类型
     * @param <T>    类引用
     * @return 类的构造器
     */
    public static <T> Constructor<T> getConstructor(Class<T> clz, Class<?>... argClz) {
        try {
            return argClz != null ? clz.getConstructor(argClz) : clz.getConstructor();
        } catch (NoSuchMethodException e) {
            log.error("Error occurred when creating instance of class: " + clz, e);
            throw new RuntimeException("Error occurred when creating instance of class: " + clz, e);
        } catch (SecurityException e) {
            log.error("Security Error occurred when getting the constructor  of class: " + clz, e);
            throw new RuntimeException("Security Error occurred when getting the constructor  of class: " + clz, e);
        }
    }
}