package com.ajaxjs.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.TimeZone;

/**
 * Debug tools.
 */
@Slf4j
public class DebugTools {
    /**
     * 打印数组以便测试
     *
     * @param arr 数组对象，可以为 null
     */
    public static void printArray(Object[] arr) {
        if (arr == null) {
            System.err.println("数组为空，null！");
            return;
        }

        if (arr.length == 0)
            System.err.println("数组不为空，但没有一个元素在内");

        log.info(Arrays.toString(arr));
    }

    /**
     * 获取操作系统名称
     */
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    /**
     * 是否调试模式（开发模式）
     */
    public static boolean isDebug;

    static {
        if (!"Asia/Shanghai".equals(System.getProperty("user.timezone"))) {
            System.err.println("当前 JVM 非中国大陆时区");
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        }

        /*
         * 有两种模式：本地模式和远程模式（自动判断） 返回 true 表示是非 linux 环境，为开发调试的环境，即 isDebug = true； 返回
         * false 表示在部署的 linux 环境下。 Linux 的为远程模式
         */
        isDebug = !(OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.indexOf("aix") > 0);

        // export AJAXJS_TEST="true"
        if ("true".equals(System.getenv("AJAXJS_TEST")))
            isDebug = true;
    }

    /**
     * 是否在运行单元测试
     */
    private static Boolean isRunningTest;

    /**
     * 检测是否在运行单元测试
     *
     * @return true=运行单元测试
     */
    public static Boolean isRunningTest() {
        if (isRunningTest == null) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

            for (StackTraceElement e : stackTrace) {
                if (e.toString().lastIndexOf("junit.runners") > -1)
                    return true;
            }

            isRunningTest = false;
        }

        return isRunningTest;
    }
}