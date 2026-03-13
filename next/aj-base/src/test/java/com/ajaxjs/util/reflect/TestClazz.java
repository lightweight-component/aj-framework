package com.ajaxjs.util.reflect;


import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.ajaxjs.util.reflect.Clazz.getConstructor;
import static com.ajaxjs.util.reflect.Clazz.newInstance;
import static org.junit.jupiter.api.Assertions.*;

class TestClazz {
    @Test
    void testGetClassByName() {
        Class<?> actual = Clazz.getClassByName("java.lang.String");
        assertEquals(String.class, actual);
    }

    @Test
    void testGetClassByName_whenClassNotFound() {
        Class<?> actual = Clazz.getClassByName("com.example.NotFoundClass");
        assertNull(actual);
    }

    @Test
    void testGetClassByName_whenClassFoundWithGenerics() {
        Class<?> actual = Clazz.getClassByName("java.util.ArrayList");
        assertEquals(ArrayList.class, actual);
    }

    @Test
    void testGetClassByInterface() {
        Class<?> actual = Clazz.getClassByInterface(List.class);
        assertEquals(List.class, actual);
    }

    @Test
    void testGetDeclaredInterface() {
        // Given
        Class<?> clz = TestClass.class;
        Type[] interfaces = {List.class, Comparable.class};
        List<Class<?>> expected = Arrays.asList(Comparable.class, List.class);

        // When
        Class<?>[] actual = Clazz.getDeclaredInterface(clz);

        // Then
//        List<Class<?>> actualInterfaces = Arrays.stream(actual)
//                .map(Class::cast)
//                .collect(Collectors.toList());
//        Assert.assertEquals(expected, actualInterfaces);
    }


    @Test
    void testNewInstance() {
        assertNotNull(newInstance(TestReflectUtil.Foo.class));
        assertNotNull(newInstance(TestReflectUtil.Foo.class, "a", "b"));
        assertNotNull(newInstance(Objects.requireNonNull(getConstructor(TestReflectUtil.Foo.class))));
        assertNotNull(newInstance(Objects.requireNonNull(getConstructor(TestReflectUtil.Foo.class, String.class, String.class)), "a", "b"));
        assertNotNull(newInstance("com.ajaxjs.util.reflect.TestReflectUtil"));
        assertNotNull(Clazz.getClassByName("com.ajaxjs.util.reflect.TestReflectUtil"));

        Class<?>[] cs = Clazz.getDeclaredInterface(ArrayList.class);
        assertNotNull(cs);
    }
}