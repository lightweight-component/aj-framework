package com.ajaxjs.business.reflection.paramnames;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * 获取构造函数或方法的参数名基类<br>
 * 当不能获取参数名的情况下,
 * {@link #returnFakeNameIfFail}为{@code false}时返回{@code null},否则返回返回arg,arg2...格式的替代名<br>
 * {@link #returnFakeNameIfFail}默认为{@code true}
 *
 * @author guyadong
 */
public abstract class BaseParameterNames {
    protected final Class<?> clazz;

    /**
     * 当获取无法参数名时是否返回arg,arg2...格式的替代名字
     */
    private boolean returnFakeNameIfFail = true;

    public BaseParameterNames setReturnFakeNameIfFail(boolean returnFakeNameIfFail) {
        this.returnFakeNameIfFail = returnFakeNameIfFail;
        return this;
    }

    /**
     * @param clazz 要构造函数或方法的参数名的类,为{@code null}时所有getParameterNames方法返回{@code null}
     */
    public BaseParameterNames(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * 获取构造函数或方法的参数名
     *
     * @param member 构造函数或方法对象
     * @return 参数名列表, 找不到返回{@code null}
     */
    protected abstract String[] doGetParameterNames(Member member);

    /**
     * 获取构造函数或方法的参数名
     *
     * @param member 构造函数或方法对象
     * @return 参数名列表, 找不到时如果{@link #returnFakeNameIfFail}为{@code true}则返回arg,arg2...格式的替代名字,否则返回{@code null}
     */
    public String[] getParameterNames(Member member) {
        String[] parameterNames = doGetParameterNames(member);
        int paramCount;

        if (member instanceof Method)
            paramCount = ((Method) member).getParameterTypes().length;
        else if (member instanceof Constructor)
            paramCount = ((Constructor<?>) member).getParameterTypes().length;
        else
            throw new IllegalArgumentException("member type must be Method or Constructor");

        if (returnFakeNameIfFail && null == parameterNames) {
            parameterNames = new String[paramCount];

            for (int i = 0; i < parameterNames.length; i++)
                parameterNames[i] = String.format("arg%d", i);
        }

        return parameterNames;
    }

    /**
     * 获取构造函数或方法的参数名<br>
     * {@code name}为{@code null}时,获取构造函数的参数名
     *
     * @param name           方法名
     * @param parameterTypes 构造函数或方法的参数类型
     * @return 参数名列表, 找不到返回{@code null}
     * @throws NoSuchMethodException
     */
    public String[] getParameterNames(String name, Class<?>[] parameterTypes) throws NoSuchMethodException {
        if (null == clazz)
            return null;

        try {
            Member member = null == name ? clazz.getConstructor(parameterTypes) : clazz.getMethod(name, parameterTypes);
            return getParameterNames(member);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * {@link #getParameterNames(String, Class[])}不显式抛出异常版本
     *
     * @param name
     * @param parameterTypes
     * @return 参数名列表, 找不到将{@link NoSuchMethodException}封装到{@link RuntimeException}抛出
     */
    public String[] getParameterNamesUnchecked(String name, Class<?>[] parameterTypes) {
        try {
            return getParameterNames(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
