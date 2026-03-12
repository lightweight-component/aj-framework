package com.ajaxjs.util.reflect;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Type Reflection Utility - Provides methods for working with Java reflection types,
 * particularly for extracting generic type information from classes, methods, and types.
 *
 * <p>This class simplifies common operations for accessing and manipulating generic types
 * in Java's reflection system, making it easier to work with parameterized types at runtime.
 */
@Slf4j
public class Types {
    /**
     * Gets the array of generic type arguments.
     *
     * @param type The Type object to extract generic type arguments from
     * @return The array of generic type arguments, or null if the specified Type is not a ParameterizedType
     */
    public static Type[] getActualType(Type type) {
        if (type instanceof ParameterizedType)
            return ((ParameterizedType) type).getActualTypeArguments();
        else {
            log.warn("{} may not be a generic", type);
            return null;
        }
    }

    /**
     * Gets the generic type arguments from a method's return type, such as String in List&lt;String&gt;
     * rather than the generic parameter T.
     *
     * @param method The method to analyze
     * @return The actual type arguments, which may be multiple
     */
    public static Type[] getGenericReturnType(Method method) {
        return getActualType(method.getGenericReturnType());
    }

    /**
     * Gets the generic type arguments from a method's return type, such as String in List&lt;String&gt;
     * rather than the generic parameter T. This method retrieves only the first type and converts it to a Class.
     *
     * @param method The method to analyze
     * @return The first actual type as a Class
     */
    public static Class<?> getGenericFirstReturnType(Method method) {
        Type[] type = getGenericReturnType(method);

        return type == null ? null : type2class(type[0]);
    }

    /**
     * Gets the generic type arguments from a class definition, such as String in List&lt;String&gt;
     *
     * @param clz The class to analyze. The class must point to an instance, see
     *            <a href="https://stackoverflow.com/questions/8436055/how-to-get-class-of-generic-type-when-there-is-no-parameter-of-it">...</a>
     * @return The actual type arguments
     */
    public static Type[] getActualType(Class<?> clz) {
        return getActualType(clz.getGenericSuperclass());
    }

    /**
     * Gets the actual class from a generic type definition
     *
     * @param clz The class to analyze
     * @return The actual class
     */
    public static Class<?> getActualClass(Class<?> clz) {
        Type[] actualType = getActualType(clz);

        return type2class(actualType[0]);
    }

    /**
     * Converts a Type interface to a Class
     *
     * @param type The Type interface to convert
     * @return The corresponding Class, or null if the type is not a Class
     */
    public static Class<?> type2class(Type type) {
        return type instanceof Class ? (Class<?>) type : null;
    }

    /*
        从 Spring 4.0 开始 Spring 中添加了 ResolvableType 工具，这个类可以更加方便的用来回去泛型信息。
        Ref: https://my.oschina.net/qq596392912/blog/3028409
     */
}