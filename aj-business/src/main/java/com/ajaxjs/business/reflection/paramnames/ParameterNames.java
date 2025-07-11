package com.ajaxjs.business.reflection.paramnames;

import com.ajaxjs.reflection.paramnames.axis2.ChainedParamReader;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * 基于解析类数据实现获取构造函数或方法的参数名<br>
 *
 * @author guyadong
 */
public class ParameterNames extends BaseParameterNames {
    private final Map<Class<?>, ChainedParamReader> readers = new HashMap<Class<?>, ChainedParamReader>();

    /**
     * @param clazz 要构造函数或方法的参数名的类,为{@code null}时所有getParameterNames方法返回{@code null}
     */
    public ParameterNames(Class<?> clazz) {
        super(clazz);

        if (null != clazz) {
            Class<?> c = clazz;

            try {
                do {
                    readers.put(c, new ChainedParamReader(c));
                } while (null != (c = c.getSuperclass()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected String[] doGetParameterNames(Member member) {
        if (null == clazz)
            return null;

        Class<?> declaringClass = member.getDeclaringClass();
        ChainedParamReader reader;

        if (null == (reader = readers.get(declaringClass)))
            throw new IllegalArgumentException(String.format("%s is not member of %s", member.toString(), declaringClass.getName()));

        String[] parameterNames = null;

        if (member instanceof Method)
            parameterNames = reader.getParameterNames((Method) member);
        else if (member instanceof Constructor)
            parameterNames = reader.getParameterNames((Constructor<?>) member);

        return parameterNames;
    }
}
