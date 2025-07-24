package com.ajaxjs.business.reflection.paramnames;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TestParamsNames {
    @Test
    public void test() throws NoSuchMethodException, SecurityException {
        outputParameterNames(BaseClassA.class.getMethod("test1", String.class));
        outputParameterNames(BaseClassA.class.getMethod("test2", String.class));
        outputParameterNames(BaseClassA.class.getMethod("test3"));
    }


    private static void outputParameterNames(Method method) {
        // 抽象方法不能正确获取参数名，只能用假名替代
        System.out.printf("%s abstract = %b\n parameter names:", method.getName(),
                Modifier.isAbstract(method.getModifiers()));
        ParameterNames pt = new ParameterNames(method.getDeclaringClass());
        String[] names = pt.getParameterNames(method);
        for (String name : names) {
            System.out.print(name + ",");
        }
        System.out.println();
    }

}
