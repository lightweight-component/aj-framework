package com.ajaxjs.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regular Expression Utility Class
 * <p>
 * Provides convenient methods for working with regular expressions in Java,
 * including matching, finding patterns, and caching compiled patterns for improved performance.
 */
public class RegExpUtils {
    /**
     * Checks if the given string matches the specified pattern
     *
     * @param pattern the compiled regular expression pattern
     * @param str     the string to test
     * @return true if the string matches the pattern, false otherwise
     */
    public static boolean isMatch(Pattern pattern, String str) {
        return pattern.matcher(str).find();
    }

    /**
     * 返回 Matcher
     *
     * @param regexp 正则
     * @param str    测试的字符串
     * @return Matcher
     */
    private static Matcher getMatcher(String regexp, String str) {
        return Pattern.compile(regexp).matcher(str);
    }

    /**
     * 使用正则的快捷方式。可指定分组
     *
     * @param regexp     正则
     * @param str        测试的字符串
     * @param groupIndex 分组 id，若为 -1 则取最后一个分组
     * @return 匹配结果
     */
    public static String regMatch(String regexp, String str, int groupIndex) {
        Matcher m = getMatcher(regexp, str);

        if (groupIndex == -1)
            groupIndex = m.groupCount();

        return m.find() ? m.group(groupIndex) : null;
    }

    /**
     * 使用正则的快捷方式
     *
     * @param regexp 正则
     * @param str    测试的字符串
     * @return 匹配结果，只有匹配第一个
     */
    public static String regMatch(String regexp, String str) {
        return regMatch(regexp, str, 0);
    }

    /**
     * 返回所有匹配项
     *
     * @param regexp 正则
     * @param str    测试的字符串
     * @return 匹配结果
     */
    public static String[] regMatchAll(String regexp, String str) {
        Matcher m = getMatcher(regexp, str);
        List<String> list = new ArrayList<>();

        while (m.find())
            list.add(m.group());

        return list.toArray(new String[0]);
    }

    /**
     * Cache for compiled regular expression patterns
     * <p>
     * Improves performance by avoiding repeated compilation of the same regular expressions
     */
    private final static Map<String, Pattern> CACHE = new ConcurrentHashMap<>();

    /**
     * Gets a compiled pattern for the given regular expression, using cache if available
     *
     * @param regexp the regular expression string
     * @return the compiled Pattern object
     */
    public static Pattern getPattern(String regexp) {
        return CACHE.computeIfAbsent(regexp, Pattern::compile);
    }

    /**
     * Determines if the entire string matches the given regular expression
     * <p>
     * Uses matches() which requires the pattern to match the entire input string
     *
     * @param regexp the regular expression to compile and use for matching
     * @param str    the string to test
     * @return true if the entire string matches the pattern, false otherwise
     */
    public static boolean match(String regexp, String str) {
        return getPattern(regexp).matcher(str).matches();
    }

    /**
     * Tests if the string contains a match of the given regular expression
     * <p>
     * Uses find() which searches for any occurrence of the pattern in the string
     *
     * @param pattern the regular expression to compile and use for testing
     * @param str     the string to test
     * @return true if the string contains a match of the pattern, false otherwise
     */
    public static boolean test(String pattern, String str) {
        return getPattern(pattern).matcher(str).find();
    }
}