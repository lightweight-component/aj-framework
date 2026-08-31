package com.ajaxjs.framework.validator.annotation;

import java.lang.annotation.*;

/**
 * 约束字符序列匹配指定正则表达式。
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Pattern {
    /**
     * 用于匹配值的正则表达式。
     *
     * @return 正则表达式
     */
    String regexp();

    /**
     * 正则表达式匹配标志。
     *
     * @return 匹配标志数组
     */
    Flag[] flags() default {};

    /**
     * 校验失败提示语或消息键。
     *
     * @return 提示语或消息键
     */
    String message() default "{pattern}";

    /**
     * 可用于 {@link java.util.regex.Pattern} 的正则匹配标志。
     */
    enum Flag {
        /**
         * 启用 UNIX 行模式。
         */
        UNIX_LINES(java.util.regex.Pattern.UNIX_LINES),
        /**
         * 启用不区分大小写匹配。
         */
        CASE_INSENSITIVE(java.util.regex.Pattern.CASE_INSENSITIVE),
        /**
         * 允许正则表达式中的空白和注释。
         */
        COMMENTS(java.util.regex.Pattern.COMMENTS),
        /**
         * 启用多行模式。
         */
        MULTILINE(java.util.regex.Pattern.MULTILINE),
        /**
         * 使点号匹配行终止符。
         */
        DOTALL(java.util.regex.Pattern.DOTALL),
        /**
         * 启用 Unicode 大小写匹配。
         */
        UNICODE_CASE(java.util.regex.Pattern.UNICODE_CASE),
        /**
         * 启用规范等价匹配。
         */
        CANON_EQ(java.util.regex.Pattern.CANON_EQ),
        /**
         * 启用 Unicode 字符类。
         */
        UNICODE_CHARACTER_CLASS(java.util.regex.Pattern.UNICODE_CHARACTER_CLASS);

        /**
         * 对应的 JDK 正则匹配标志位。
         */
        private final int value;

        /**
         * 使用 JDK 正则标志位创建枚举项。
         *
         * @param value JDK 正则标志位
         */
        Flag(int value) {
            this.value = value;
        }

        /**
         * 获取 JDK 正则匹配标志位。
         *
         * @return 正则匹配标志位
         */
        public int getValue() {
            return value;
        }
    }
}
