package com.ajaxjs.util;


import com.ajaxjs.util.ConvertBasicValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestCollUtils {
    private List<String> nonEmptyList;
    private List<String> emptyList;
    private List<String> nullList;

    /**
     * 合并两个数组
     *
     * @param <T>    范型，数组中元素的类别
     * @param first  第一个数组
     * @param second 第二个数组
     * @return 合并后的数组，first数组在前，second数组在后
     */
    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);

        return result;
    }

    /**
     * 将多个数组合并在一起
     * 忽略 null 的数组
     *
     * @param <T>    数组元素类型
     * @param arrays 数组集合
     * @return 合并后的数组
     */
    @SafeVarargs
    public static <T> T[] addAll(T[]... arrays) {
        if (arrays.length == 1)
            return arrays[0];

        int length = 0;
        for (T[] array : arrays) {
            if (null != array)
                length += array.length;
        }

        T[] result = newArray(arrays.getClass().getComponentType().getComponentType(), length);
        length = 0;

        for (T[] array : arrays) {
            if (null != array) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }

        return result;
    }

    /**
     * 新建一个空数组
     *
     * @param <T>           数组元素类型
     * @param componentType 元素类型
     * @param newSize       大小
     * @return 空数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(Class<?> componentType, int newSize) {
        return (T[]) Array.newInstance(componentType, newSize);
    }

    /**
     * 数组转换为 List
     *
     * @param <E>      范型，List 中元素的类别
     * @param elements 需要转换为 List 的数组
     * @return 转换后的List对象
     */
    @SafeVarargs
    public static <E> List<E> arrayList(E... elements) {
        List<E> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);

        return list;
    }

    /**
     * 在给定列表中查找第一个满足条件的元素
     *
     * @param <T>    范型，列表中元素的类别
     * @param list   给定列表
     * @param filter 过滤元素的条件
     * @return 第一个满足条件的元素，如果没有符合条件的元素，则返回 null
     */
    public static <T> T findOne(List<T> list, Predicate<T> filter) {
        return list.stream().filter(filter).findFirst().orElse(null);
    }

    /**
     * 在给定列表中查找满足条件的所有元素并封装成 List
     *
     * @param <T>    范型，列表中元素的类别
     * @param list   给定列表
     * @param filter 过滤元素的条件
     * @return 符合条件的元素封装的 List 集合
     */
    public static <T> List<T> findSome(List<T> list, Predicate<T> filter) {
        return list.stream().filter(filter).collect(Collectors.toList());
    }

    @BeforeEach
    public void setUp() {
        nonEmptyList = new ArrayList<>();
        nonEmptyList.add("A");
        nonEmptyList.add("B");

        emptyList = new ArrayList<>();

        nullList = null;
    }

    @Test
    public void concat_TwoArrays_ReturnsConcatenatedArray() {
        String[] first = {"A", "B"};
        String[] second = {"C", "D"};
        String[] expected = {"A", "B", "C", "D"};

        String[] result = concat(first, second);
        assertArrayEquals(expected, result, "Expected concatenated array");
    }

    @Test
    public void addAll_MultipleArrays_ReturnsConcatenatedArray() {
        Integer[] first = {1, 2};
        Integer[] second = {3, 4};
        Integer[] third = {5, 6};
        Integer[] expected = {1, 2, 3, 4, 5, 6};

        Integer[] result = addAll(first, second, third);
        assertArrayEquals(expected, result, "Expected concatenated array");
    }

    @Test
    public void newArray_CreatesNewArray() {
        Integer[] array = newArray(Integer.class, 5);
        assertNotNull(array, "Expected a new array");
        assertEquals(5, array.length, "Expected array of length 5");
    }

    @Test
    public void arrayList_FromArray_ReturnsArrayList() {
        String[] elements = {"A", "B", "C"};
        List<String> expected = new ArrayList<>();
        expected.add("A");
        expected.add("B");
        expected.add("C");

        List<String> result = arrayList(elements);
        assertEquals(expected, result, "Expected ArrayList with same elements");
    }

    @Test
    public void findOne_FirstMatchingElement_ReturnsElement() {
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");

        String result = findOne(list, s -> s.startsWith("B"));
        assertEquals("Banana", result, "Expected 'Banana' as the first matching element");
    }

    @Test
    public void findOne_NoMatchingElement_ReturnsNull() {
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");

        String result = findOne(list, s -> s.startsWith("D"));
        assertNull(result, "Expected null as there is no matching element");
    }

    @Test
    public void findSome_MatchingElements_ReturnsList() {
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");
        list.add("Apricot");

        List<String> result = findSome(list, s -> s.startsWith("A"));
        assertNotNull(result, "Expected a non-null list");
        assertTrue(result.contains("Apple"), "Expected list to contain 'Apple'");
        assertTrue(result.contains("Apricot"), "Expected list to contain 'Apricot'");
    }

    @Test
    public void findSome_NoMatchingElements_ReturnsEmptyList() {
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");

        List<String> result = findSome(list, s -> s.startsWith("D"));
        assertNotNull(result, "Expected a non-null list");
        assertTrue(result.isEmpty(), "Expected empty list as there are no matching elements");
    }

    @Test
    public void intList2Arr_ConvertsListToIntArray() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        int[] expected = {1, 2, 3};
        int[] result = ConvertBasicValue.intList2Arr(list);
        assertArrayEquals(expected, result, "Expected int array with same elements");
    }

    @Test
    public void stringArr2intArr_ConvertsStringToIntArray() {
        String input = "1,2,3";
        int[] expected = {1, 2, 3};
        int[] result = ConvertBasicValue.stringArr2intArr(input);
        assertArrayEquals(expected, result, "Expected int array with parsed elements");
    }
}
