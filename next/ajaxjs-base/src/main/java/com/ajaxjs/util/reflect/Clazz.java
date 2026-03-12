package com.ajaxjs.util.reflect;

import com.ajaxjs.util.CommonConstant;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class Reflection Utility - Provides comprehensive methods for class reflection operations
 * including class loading, instantiation, constructor management, and type conversion.
 *
 * <p>This class simplifies common reflection tasks in Java, making it easier to work with
 * dynamic class loading, object creation, and interface implementations at runtime.
 */
@Slf4j
public class Clazz {
    /**
     * Gets a class object by its fully qualified name.
     *
     * @param clzName The fully qualified class name. Note special handling for inner classes.
     * @return The corresponding class object
     */
    public static Class<?> getClassByName(String clzName) {
        try {
            return Class.forName(clzName);
        } catch (ClassNotFoundException e) {
            log.error("Class:" + clzName + " not Found.", e);
            throw new RuntimeException("Class:" + clzName + " not Found.");
        }
    }

    /**
     * Gets a class object by its fully qualified name with strong type casting.
     *
     * @param clzName The fully qualified class name
     * @param clz     The target type to cast to
     * @param <T>     The class reference type
     * @return The corresponding class object cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClassByName(String clzName, Class<T> clz) {
        Class<?> c = getClassByName(clzName);

        return (Class<T>) c;
    }

    /**
     * Converts an array of objects to an array of their corresponding class objects.
     * This function transforms a variable argument list into a Class array where
     * each element represents the class type of the corresponding input argument.
     *
     * @param args The variable argument list
     * @return The array of corresponding class objects
     */
    public static Class<?>[] args2class(Object[] args) {
        Class<?>[] clazz = new Class[args.length];

        for (int i = 0; i < args.length; i++)
            clazz[i] = args[i].getClass();

        return clazz;
    }

    /**
     * Gets the class object for a given interface type.
     *
     * @param type The interface type
     * @return The class object for the interface
     */
    public static Class<?> getClassByInterface(Type type) {
        String className = type.toString();
        className = className.replaceAll("<.*>$", CommonConstant.EMPTY_STRING).replaceAll("(class|interface)\\s", CommonConstant.EMPTY_STRING); // 不要泛型的字符

        return getClassByName(className);
    }

    /**
     * Gets all interfaces implemented by a given class.
     *
     * @param clz The target class
     * @return All interfaces implemented by the class
     */
    public static Class<?>[] getDeclaredInterface(Class<?> clz) {
        List<Class<?>> fields = new ArrayList<>();

        for (; clz != Object.class; clz = clz.getSuperclass()) {
            Class<?>[] currentInterfaces = clz.getInterfaces();
            fields.addAll(Arrays.asList(currentInterfaces));
        }

        return fields.toArray(new Class[0]);
    }

    /**
     * Creates an instance of a class with optional constructor arguments.
     * This function creates an instance based on the given class object and constructor arguments.
     * If the class is an interface, an IllegalArgumentException is thrown.
     * If no arguments are provided, the default no-args constructor is used.
     * If arguments are provided, the constructor matching those argument types is used.
     *
     * @param clz  The class object
     * @param args Optional arguments for the constructor
     * @param <T>  The class reference type
     * @return The created object instance
     */
    public static <T> T newInstance(Class<T> clz, Object... args) {
        if (clz.isInterface())
            throw new IllegalArgumentException("所传递的 class 类型参数为接口 " + clz + "，无法实例化");

        if (args == null || args.length == 0) {
            try {
                return newInstance(clz.getDeclaredConstructor());
            } catch (NoSuchMethodException e) {
                log.error("The constructor of this class " + clz.getName() + " is not found.", e);
                throw new RuntimeException("The constructor of this class " + clz.getName() + " is not found.", e);
            }
        }

        Constructor<T> constructor = getConstructor(clz, Clazz.args2class(args));// 获取构造器

        return newInstance(constructor, args);
    }

    /**
     * Creates an instance using a specific constructor and arguments.
     * This function creates an instance of the class using the provided constructor
     * and argument list through reflection.
     *
     * @param constructor The class constructor to use
     * @param args        Optional arguments for the constructor
     * @param <T>         The class reference type
     * @return The created object instance
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
     * Checks if a class has any constructors with parameters.
     * This function determines whether the provided class has any constructors that
     * accept parameters by examining all available constructors.
     *
     * @param clz The class object to check
     * @return true if the class has at least one constructor with parameters
     */
    public static boolean hasArgsCon(Class<?> clz) {
        Constructor<?>[] constructors = clz.getConstructors();

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length != 0)
                return true;
        }

        return false;
    }

    /**
     * Creates an instance from a fully qualified class name and casts it to an interface type.
     *
     * @param className The fully qualified class name
     * @param clazz     The interface type to cast to
     * @param <T>       The class reference type
     * @return The created object instance cast to the interface type
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className, Class<T> clazz) {
        Class<?> clz = getClassByName(className);

        return clazz != null ? (T) newInstance(clz) : null;
    }

    /**
     * Creates an instance from a fully qualified class name with optional constructor arguments.
     * This function uses reflection to load the class and create an instance based on the
     * provided class name and constructor arguments.
     *
     * @param clzName The fully qualified class name
     * @param args    Arguments for the constructor
     * @return The created object instance as an Object type
     */
    public static Object newInstance(String clzName, Object... args) {
        Class<?> clazz = getClassByName(clzName);

        return newInstance(clazz, args);
    }

    /**
     * Gets a constructor for a class, supporting overloaded constructors with different parameters.
     * This function retrieves a constructor based on the provided class object and optional parameter types.
     * If parameter types are provided, the matching constructor is returned;
     * otherwise, the no-args constructor is returned.
     *
     * @param clz    The class object
     * @param argClz The parameter types for the desired constructor
     * @param <T>    The class reference type
     * @return The requested constructor
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