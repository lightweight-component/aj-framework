/*
 * Copyright (C) 2025 Frank Cheung<frank@ajaxjs.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ajaxjs.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String Utility Class - Provides various string manipulation and formatting operations
 * including URL concatenation, template substitution, joining arrays, and string padding.
 */
public class StrUtil {
    /**
     * 统计文本中某个字符串出现的次数
     *
     * @param str   输入的字符串
     * @param _char 某个字符
     * @return 出现次数
     */
    public static int charCount(String str, String _char) {
        int count = 0, index = 0;

        while (true) {
            index = str.indexOf(_char, index + 1);

            if (index > 0)
                count++;
            else
                break;
        }

        return count;
    }

    /**
     * 字符串左填充方法
     * <p>
     * 例如: leftPad("12345", 10, "@")，输出："@@@@@12345"
     *
     * @param str   待填充字符串
     * @param len   总长度
     * @param _char 填充字符
     * @return 左填充后的字符串
     */
    public static String leftPad(String str, int len, String _char) {
        return String.format("%" + len + "s", str).replaceAll("\\s", _char);
    }

    private static final Pattern TPL_PATTERN = Pattern.compile("\\$\\{\\w+}");

    /**
     * 简单模板替换方法。根据 Map 中的数据进行替换
     *
     * @param template 待替换的字符串模板
     * @param params   存放替换数据的 Map
     * @return 替换后的字符串
     */
    public static String simpleTpl(String template, Map<String, Object> params) {
        StringBuffer sb = new StringBuffer();
        Matcher m = TPL_PATTERN.matcher(template);

        while (m.find()) {
            String param = m.group();
            // 获取要替换的键名，即去除 '${' 和 '}' 后的部分
            Object value = params.get(param.substring(2, param.length() - 1));
            m.appendReplacement(sb, value == null ? CommonConstant.EMPTY_STRING : value.toString());// 替换键值对应的值，若值为 null，则置为空字符串
        }

        m.appendTail(sb);

        return sb.toString();
    }

    /**
     * 简单模板替换方法。根据 Map 中的数据进行替换。
     * 与 simpleTpl 方法的区别在于这里将 null 值替换为字符串 "null"。
     *
     * @param template 待替换的字符串模板
     * @param data     存放替换数据的 Map
     * @return 替换后的字符串
     */
    public static String simpleTpl2(String template, Map<String, Object> data) {
        String result = template;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null)
                value = "null";

            String placeholder = "#{" + key + "}";
            result = result.replace(placeholder, value.toString());
        }

        return result;
    }

    /**
     * 简单模板替换方法。根据 JavaBean 中的数据进行替换。
     *
     * @param template 待替换的字符串模板
     * @param data     存放替换数据的 JavaBean 对象
     * @return 替换后的字符串
     */
    public static String simpleTpl(String template, Object data) {
        String result = template;

        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(data.getClass()).getPropertyDescriptors()) {
                String name = descriptor.getName();
                Object value = descriptor.getReadMethod().invoke(data);

                if (value == null)
                    value = "null";

                String placeholder = "#{" + name + "}";
                result = result.replace(placeholder, value.toString());
            }
        } catch (InvocationTargetException | IllegalAccessException | IntrospectionException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * 将列表中的元素使用指定的分隔符连接成一个字符串，并返回连接后的字符串
     *
     * @param <T>  数组类型
     * @param list 任何类型的列表
     * @param str  字符串类型的分隔符
     * @return 连接后的字符串
     */
    public static <T> String joinAnyList(List<T> list, String str) {
        Object[] objectArray = list.toArray();
        @SuppressWarnings("unchecked")
        T[] array = Arrays.copyOf(objectArray, objectArray.length, (Class<? extends T[]>) objectArray.getClass());

        return join(array, str);
    }

    /**
     * 将数组中的元素使用指定的分隔符连接成一个字符串，并返回连接后的字符串
     *
     * @param <T> 数组类型
     * @param arr 任何类型的数组
     * @param str 字符串类型的分隔符
     * @return 连接后的字符串
     */
    public static <T> String join(T[] arr, String str) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0, len = arr.length; i < len; i++) {
            String s = arr[i].toString();

            if (i != (len - 1))
                sb.append(s).append(str);
            else
                sb.append(s);
        }

        return sb.toString();
    }

    /**
     * 将字符串数组转为字符串，可自定义分隔符及字符串模板
     *
     * @param arr 字符串数组
     * @param tpl 字符串格式化模板
     * @param str 用于分隔字符串的字符
     * @return 拼接结果字符串
     */
    public static String join(String[] arr, String tpl, String str) {
        return join(Arrays.asList(arr), tpl, str);
    }

    /**
     * 将字符串列表转为字符串，可自定义分隔符
     *
     * @param list 字符串列表
     * @param str  用于分隔字符串的字符
     * @return 拼接结果字符串
     */
    public static String join(List<String> list, String str) {
        return join(list, null, str);
    }

    /**
     * 将字符串列表转为字符串，可自定义分隔符及字符串格式化模板
     *
     * @param list 字符串列表
     * @param tpl  字符串格式化模板
     * @param str  用于分隔字符串的字符
     * @return 拼接结果字符串
     */
    public static String join(List<String> list, String tpl, String str) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0, len = list.size(); i < len; i++) {
            String s = list.get(i);

            if (tpl != null)
                s = String.format(tpl, s);

            if (i != (len - 1))
                sb.append(s).append(str);
            else
                sb.append(s);
        }

        return sb.toString();
    }

    /**
     * 判断一个字符串是否属于指定的字符串数组中
     *
     * @param word 待判断字符串
     * @param arr  指定字符串数组
     * @return 如果字符串属于数组中，则返回 true；否则返回 false
     */
    public static boolean isWordOneOfThem(String word, String[] arr) {
        for (String str : arr) {
            if (word.equals(str))
                return true;
        }

        return false;
    }

    /**
     * 判断一个字符串是否属于指定的字符串列表中
     *
     * @param word 待判断字符串
     * @param list 指定字符串列表
     * @return 如果字符串属于列表中，则返回 true；否则返回 false
     */
    public static boolean isWordOneOfThem(String word, List<String> list) {
        return isWordOneOfThem(word, list.toArray(new String[0]));
    }
}