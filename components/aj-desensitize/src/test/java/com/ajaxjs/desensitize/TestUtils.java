package com.ajaxjs.desensitize;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static com.ajaxjs.desensitize.Utils.getAllFields;
import static com.ajaxjs.desensitize.Utils.remove;

public class TestUtils {
    @Test
    void testRemove() {
        Integer[] numbers = {1, 2, 3, 4, 5};

        Integer[] updatedNumbers = remove(numbers, 2); // Remove element at index 2 (value 3)
        System.out.println(java.util.Arrays.toString(updatedNumbers)); // Output: [1, 2, 4, 5]
    }

    // 示例类
    class Parent {
        private String parentField;
    }

    class Child extends Parent {
        private int childField;
    }

    @Test
    void testGetAllFields() {
        Class<?> clazz = Child.class;
        List<Field> allFields = getAllFields(clazz);

        // 打印所有字段
        allFields.forEach(System.out::println);
    }

    @Test
    void testGetField() {
        try {
            Field field = Utils.getField(Child.class, "childField", true);
            System.out.println("Found field: " + field.getName());

            // 创建类实例并设置字段值
            Child instance = new Child();
            field.set(instance, 12);
            System.out.println("Set value: " + field.get(instance));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
