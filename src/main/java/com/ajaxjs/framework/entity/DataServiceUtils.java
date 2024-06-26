package com.ajaxjs.framework.entity;

import com.ajaxjs.framework.DiContextUtil;
import com.ajaxjs.util.convert.ConvertBasicValue;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DataServiceUtils {
    /**
     * 获取查询字符串参数。
     * 从当前请求中提取查询参数，并将其转换为 Map 形式返回，键为参数名，值为参数值。
     * 如果参数值为数组，则只取第一个值进行处理。处理过程中，会对参数值进行 SQL 注入的防范处理。
     * 如果没有查询参数，则返回 null。
     *
     * @return 包含查询参数的Map，如果不存在查询参数则返回 null
     */
    public static Map<String, Object> getQueryStringParams() {
        // 从DiContextUtil中获取当前的HttpServletRequest对象
        HttpServletRequest request = DiContextUtil.getRequest();
        assert request != null;
        // 获取请求中的所有参数，包括参数名和参数值的数组
        Map<String, String[]> parameterMap = request.getParameterMap();

        if (ObjectUtils.isEmpty(parameterMap)) // 如果参数Map为空，则直接返回 null
            return null;

        // 初始化一个Map用于存储处理后的参数
        Map<String, Object> params = new HashMap<>();
        // 遍历参数Map，将参数值转换为 Java 基本类型，并处理可能的 SQL 注入
        parameterMap.forEach((key, value) -> params.put(key, value == null ? null : ConvertBasicValue.toJavaValue(escapeSqlInjection(value[0]))));

        return params;
    }

    private static final Pattern PATTERN = Pattern.compile("(?i)select|update|delete|insert|drop|truncate|union|\\*|--|;");

    /**
     * 过滤输入字符串以避免 SQ L注入攻击。
     * 该方法通过正则表达式匹配并移除可能导致SQL注入的特殊字符或关键字。
     * 使用该方法对用户输入进行清理，可以增强系统的安全性。
     *
     * @param input 待过滤的字符串，通常是用户输入
     * @return 过滤后的字符串，移除了可能的 SQL 注入关键字或字符
     */
    public static String escapeSqlInjection(String input) {
        return PATTERN.matcher(input).replaceAll("");
    }
}