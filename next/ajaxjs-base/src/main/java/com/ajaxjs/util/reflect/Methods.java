package com.ajaxjs.util.reflect;

import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Reflection Utility for Methods - Provides comprehensive method reflection operations
 * including method discovery, invocation, static method handling, and interface default methods.
 *
 * <p>This class supports method lookup with various strategies including
 * - Direct method lookup by name and parameters
 * - Upcasting search for parameter type matching
 * - Interface method resolution
 * - Static method invocation
 * - Default method handling for interfaces
 */
@Slf4j
public class Methods {
    /**
     * Get a declared method from a class by method name.
     * This method searches only for methods declared directly in the specified class.
     *
     * @param clz        The class to search for methods
     * @param methodName The name of the method to find
     * @return The declared method, or null if method doesn't exist
     * @throws RuntimeException if the method is not found
     */
    public static Method getDeclaredMethod(Class<?> clz, String methodName) {
        try {
            return clz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            log.warn("No Such Method Exception " + methodName, e);
            throw new RuntimeException("No Such Method Exception " + methodName, e);
        }
    }

    /**
     * Get a method object from a class or instance with parameter types, supporting overloaded methods.
     * This method searches for public methods including inherited ones.
     *
     * @param obj    Can be either a class object or an instance object
     * @param method The method name to search for
     * @param args   Explicit parameter type list for method overloading resolution
     * @return The matching method object, or null if not found
     */
    public static Method getMethod(Object obj, String method, Class<?>... args) {
        Class<?> cls = obj instanceof Class ? (Class<?>) obj : obj.getClass();

        try {
            return ObjectHelper.isEmpty(args) ? cls.getMethod(method) : cls.getMethod(method, args);
        } catch (NoSuchMethodException | SecurityException e) {
//            StringBuilder str = new StringBuilder();
//
//            for (Class<?> clz : args)
//                str.append(clz.getName());

//            log.warn("类找不到这个方法 {}.{}({})。", cls.getName(), method, str.toString().isEmpty() ? "void" : str.toString());
            return null;
        }
    }

    /**
     * Find a method by name and argument objects, converting arguments to their types.
     * Note: This method doesn't support upcasting, so parameter types must match exactly.
     * For upcasting support, use getMethodByUpCastingSearch().
     *
     * @param obj    Instance object to search methods from
     * @param method Method name to find
     * @param args   Argument objects used to determine parameter types
     * @return Matching method object, or null if not found
     */
    public static Method getMethod(Object obj, String method, Object... args) {
        if (!ObjectHelper.isEmpty(args))
            return getMethod(obj, method, Clazz.args2class(args));
        else
            return getMethod(obj, method);
    }

    /**
     * Find a method by name with automatic parameter type upcasting support.
     * This method searches through the class hierarchy for compatible parameter types.
     * Supports only single parameter methods currently.
     *
     * @param clz    Class object to search methods from
     * @param method Method name to find
     * @param arg    Argument object (must be an object, not a Class) for parameter type matching
     * @return Matching method object, or null if not found
     */
    public static Method getMethodByUpCastingSearch(Class<?> clz, String method, Object arg) {
        for (Class<?> clazz = arg.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                // return cls.getDeclaredMethod(methodName, clazz);
                return clz.getMethod(method, clazz); // 用 getMethod 代替更好？
            } catch (NoSuchMethodException | SecurityException e) {
                // 这里的异常不能抛出去。 如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(), 最后就不会进入到父类中了
            }
        }

        return null;
    }

    /**
     * 循环 object 向上转型（接口）
     *
     * @param clz    主类
     * @param method 方法名称
     * @param arg    参数对象，可能是子类或接口，所以要在这里找到对应的方法，当前只支持单个参数
     * @return 方法对象
     */
    public static Method getDeclaredMethodByInterface(Class<?> clz, String method, Object arg) {
        Method methodObj;

        for (Class<?> clazz = arg.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            Type[] interfaces = clazz.getGenericInterfaces();

            if (interfaces.length != 0) { // 有接口！
                try {
                    for (Type _interface : interfaces) {
                        // 旧方法，现在不行，不知道之前怎么可以的 methodObj = hostClazz.getDeclaredMethod(method, (Class<?>)_interface);
                        // methodObj = cls.getMethod(methodName,
                        // ReflectNewInstance.getClassByInterface(_interface));
                        methodObj = getSuperClassDeclaredMethod(clz, method, Clazz.getClassByInterface(_interface));

                        if (methodObj != null)
                            return methodObj;
                    }
                } catch (Exception e) {
                    log.warn("循环 object 向上转型（接口）异常 ", e);
                    throw new RuntimeException("循环 object 向上转型（接口）异常 ", e);
                }
            }
            //			else {
            // 无实现的接口
            //			}
        }

        return null;
    }

    /**
     * 查找对象父类身上指定的方法
     *
     * @param clz    主类
     * @param method 方法名称
     * @param argClz 参数类引用
     * @return 匹配的方法对象，null 表示找不到
     */
    public static Method getSuperClassDeclaredMethod(Class<?> clz, String method, Class<?> argClz) {
        for (; clz != Object.class; clz = clz.getSuperclass()) {
            try {
                return clz.getDeclaredMethod(method, argClz);
            } catch (NoSuchMethodException | SecurityException ignored) {
            }
        }

        return null;
    }

    /**
     * 查找对象父类身上指定的方法（注意该方法不需要校验参数类型是否匹配，故有可能不是目标方法，而造成异常，请谨慎使用）
     *
     * @param clz    主类
     * @param method 方法名称
     * @return 匹配的方法对象，null 表示找不到
     */
    public static Method getSuperClassDeclaredMethod(Class<?> clz, String method) {
        for (; clz != Object.class; clz = clz.getSuperclass()) {
            for (Method m : clz.getDeclaredMethods()) {
                if (m.toString().contains(method))
                    return m;
            }
        }

        return null;
    }

    /**
     * 获取所有父类
     *
     * @param clz 类对象
     * @return 所有父类
     */
    public static Class<?>[] getAllSuperClazz(Class<?> clz) {
        List<Class<?>> clzList = new ArrayList<>();

        //		while (clz != null) {
        //			clz = clazz.getSuperclass();
        //
        //			if (clz != null && clz != Object.class)
        //				clzList.add(clz);
        //		}
        for (; clz != Object.class; clz = clz.getSuperclass()) {
            if (clz == null)
                break;
            else
                clzList.add(clz);
        }

        if (!clzList.isEmpty())
            clzList.remove(0); // 排除自己

        return clzList.toArray(new Class[0]);
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
                log.error("反射执行方法异常！所在类[{}] 方法：[{}]", instance.getClass().getName(), method.getName());

                throw e;
            }

            throw e1;
        }
    }

    /**
     * 获取实际抛出的那个异常对象。 InvocationTargetException 太过于宽泛，在 trouble
     * shouting的时候，不能给人非常直观的信息 AOP 的缘故，不能直接捕获原来的异常，要不断 e.getCause()....
     *
     * @param e 异常对象
     * @return 实际异常对象
     */
    public static Throwable getUnderLayerErr(Throwable e) {
        while (e.getClass().equals(InvocationTargetException.class) || e.getClass().equals(UndeclaredThrowableException.class))
            e = e.getCause();

        return e;
    }

    /**
     * 获取实际抛出的那个异常对象，并去掉前面的包名。
     *
     * @param e 异常对象
     * @return 实际异常对象信息
     */
    public static String getUnderLayerErrMsg(Throwable e) {
        String msg = getUnderLayerErr(e).toString();

        return msg.replaceAll("^[^:]*:\\s?", CommonConstant.EMPTY_STRING);
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
            log.warn("Error occurred when executing method: " + method, e);
            return null;
        }
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
        Class<?>[] clazz = Clazz.args2class(args);
        Method methodObj = getMethod(instance.getClass(), method, clazz);

        return methodObj != null ? executeMethod(instance, methodObj, args) : null;
    }

    /**
     * 调用方法。 注意获取方法对象，原始类型和包装类型不能混用，否则得不到正确的方法， 例如 Integer 不能与 int 混用。 这里提供一个
     * argType 的参数，指明参数类型为何。
     *
     * @param instance 对象实例
     * @param method   方法名称
     * @param argType  参数类型
     * @param argValue 参数值
     * @return 执行结果
     */
    public static Object executeMethod(Object instance, String method, Class<?> argType, Object argValue) {
        Method m = getMethod(instance, method, argType);

        if (m != null)
            return executeMethod(instance, m, argValue);

        return null;
    }

    /**
     * 执行静态方法
     *
     * @param method 方法对象
     * @param args   方法参数列表
     * @return 执行结果
     */
    public static Object executeStaticMethod(Method method, Object... args) {
        if (isStaticMethod(method)) {
            try {
                return executeMethod_Throwable(new Object(), method, args);
            } catch (Throwable e) {
                log.warn("Error when executing static method: " + method, e);
                throw new RuntimeException("Error when executing static method: " + method, e);
            }
        } else
            log.warn("This is not a static method: {}", method);

        return null;
    }

    /**
     * 是否静态方法
     *
     * @param method 方法对象
     * @return true 表示为静态方法
     */
    public static boolean isStaticMethod(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * 通过反射调用接口的默认方法。
     * <p>
     * 此方法旨在提供一种通过反射机制调用Java 8及以上版本接口中默认方法的手段。
     * 它绕过了直接调用默认方法需要实例化一个类的限制，通过MethodHandles和反射机制实现。
     * <p>
     * 调用 Interface 的 default 方法
     * <a href="https://www.jianshu.com/p/63691220f81f">...</a>
     * <a href="https://link.jianshu.com/?t=http://stackoverflow.com/questions/22614746/how-do-i-invoke-java-8-default-methods-refletively">...</a>
     *
     * @param proxy  接口的代理实例，用于调用默认方法。
     * @param method 要调用的默认方法的Method对象。
     * @param args   调用方法时所需的参数数组。
     * @return 调用默认方法后的返回值。
     * @throws RuntimeException 如果在调用过程中发生任何异常，将会抛出运行时异常。
     */
    public static Object executeDefault(Object proxy, Method method, Object[] args) {
        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);

            Class<?> declaringClass = method.getDeclaringClass();
            int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE;

            return constructor.newInstance(declaringClass, allModes)
                    .unreflectSpecial(method, declaringClass)
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        } catch (Throwable e) {
            log.warn("Error when executing default method: " + method, e);
            throw new RuntimeException(e);
        }
    }
}