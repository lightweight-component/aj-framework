//package com.ajaxjs.util;
//
//
//import java.lang.invoke.MethodHandle;
//import java.lang.invoke.MethodHandles;
//import java.time.LocalDate;
//import java.time.LocalTime;
//
//public class EasyLogger {
//    enum Level {
//        INFO, WARN, ERROR
//    }
//
//    private static final String INFO_TPL = "%s %s \033[32;4mINFO\033[0m \033[36;4m%s\033[0m : %s";
//    private static final String WARN_TPL = "%s %s \033[33;4mWARN\033[0m \033[36;4m%s\033[0m : %s";
//    private static final String ERROR_TPL = "%s %s \033[31;4mERROR\033[0m \033[36;4m%s\033[0m : %s";
//
//    @SuppressWarnings("SpellCheckingInspection")
//    private static final String DELIM_STR = "{}";
//
//    private static final Object[] EMPTY_ARGS = new Object[0];
//
//    private static void print(Level level, String format, Object... args) {
//        String tpl;
//
//        if (level == Level.INFO)
//            tpl = INFO_TPL;
//        else if (level == Level.WARN)
//            tpl = WARN_TPL;
//        else
//            tpl = ERROR_TPL;
//
//        if (null == args)
//            args = EMPTY_ARGS;
//
//        StringBuilder buffer = new StringBuilder(format.length() + 64);
//        int beginIndex = 0, endIndex, count = 0;
//
//        while ((endIndex = format.indexOf(DELIM_STR, beginIndex)) >= 0) {
//            buffer.append(format, beginIndex, endIndex);
//
//            try {
//                buffer.append(args[count++]);
//            } catch (IndexOutOfBoundsException e) {
//                buffer.append("null"); // 数组越界时对应占位符填null
//            }
//            beginIndex = endIndex + DELIM_STR.length();
//        }
//
//        buffer.append(format.substring(beginIndex));
//
//        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[3];
//        String declaringClass = getPrivateField(stackTrace, "declaringClass", String.class);
//        String l = declaringClass + "#" + stackTrace.getLineNumber();
//        String str = String.format(tpl, LocalDate.now(), LocalTime.now(), l, buffer);
//
//        if (level == Level.INFO || level == Level.WARN)
//            System.out.println(str);
//        else
//            System.err.println(str);
//    }
//
//    /**
//     * 控制台打印 WARN 日志
//     *
//     * @param format 待打印
//     */
//    public static void info(String format, Object... args) {
//        print(Level.INFO, format, args);
//    }
//
//    /**
//     * 控制台打印 ERROR 日志
//     *
//     * @param format 待打印
//     */
//    public static void warn(String format, Object... args) {
//        print(Level.WARN, format, args);
//    }
//
//    /**
//     * 控制台打印 INFO 日志
//     *
//     * @param format 待打印
//     */
//    public static void error(String format, Object... args) {
//        print(Level.ERROR, format, args);
//    }
//
//    /**
//     * 获取 private 字段（JDK11+）
//     *
//     * @param clz       类
//     * @param fieldName 字段名
//     * @param fieldType 字段类型
//     * @param <T>       字段类型
//     * @return 字段的值
//     */
//    @SuppressWarnings("unchecked")
//    public static <T> T getPrivateField(Object instance, String fieldName, Class<T> fieldType) {
//        MethodHandles.Lookup lookup;
//        Class<?> clz = instance.getClass();
//
//        try {
//            lookup = MethodHandles.privateLookupIn(clz, MethodHandles.lookup());
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException("非法访问类： " + clz, e);
//        }
//
//        MethodHandle mh;
//
//        try {
//            mh = lookup.findGetter(clz, fieldName, fieldType);
//        } catch (NoSuchFieldException e) {
//            throw new RuntimeException("找不到该字段 " + fieldName, e);
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException("非法访问字段： " + fieldName, e);
//        }
//
//        try {
//            return (T) mh.invoke(instance);
//        } catch (Throwable e) {
//            throw new RuntimeException("获取 private 字段异常", e);
//        }
//    }
//}
