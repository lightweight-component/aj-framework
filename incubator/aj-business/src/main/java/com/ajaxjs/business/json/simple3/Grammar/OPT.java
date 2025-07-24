package com.ajaxjs.business.json.simple3.Grammar;


import com.ajaxjs.business.json.simple3.Lexical.LeptType;

import java.lang.reflect.Method;

public class OPT {
    public static final Method VAL = getMethod("val");
    public static final Method ARRAV = getMethod("arrav");
    public static final Method OBJAK = getMethod("objak");
    public static final Method OBJAV = getMethod("objav");
    public static final Method ARRS = getMethod("arrs");
    public static final Method OBJS = getMethod("objs");

    public static Method getMethod(String methodName) {
        try {
            /*
             * public Method getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException
             *返回一个 Method 对象，它反映此 Class对象所表示的类或接口的指定公共成员方法。
             * name - 这是方法的名称。
             * parameterTypes - 这是参数列表,按声明顺序标识该方法形参类型的Class对象的一个数组。
             *如果 parameterTypes 为null，则按空数组处理。
             */
            return Operator.class.getMethod(methodName, Integer.class, Integer.class, LeptType.class);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }
}
