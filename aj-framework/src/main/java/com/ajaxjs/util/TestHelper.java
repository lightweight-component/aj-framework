package com.ajaxjs.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestHelper {
    /*
     * 简单统计 Java 方法中每行代码的执行时间
     * 在需要调试的方法中隔几行插入 Timer.set (数字或字符) 方法，程序结束前调用 Timer.print () 方法就行了。
     * 因为目的只是排查方法执行时间，调试完成后可以将 set () 方法注释掉，然后利用 Eclipse 的查错功能将所有 Timer.set () 方法找出来删除。
     * 如果不介意方法中的垃圾代码，也可以直接将 set () 方法体改为 "return;"，对性能影响不大 (当参数为数字时)。
     * https://my.oschina.net/drinkjava2/blog/1622179
     */
    private static String lastMark = "start";
    private static long lastTime = System.nanoTime();
    private static final Map<String, Long> timeMap = new LinkedHashMap<>();
    private static final Map<String, Long> timeHappenCount = new LinkedHashMap<>();

    public static void setTimer(int mark) {
        setTimer("" + mark);
    }

    public static void setTimer(String mark) {
        long thisTime = System.nanoTime();
        String key = lastMark + "->" + mark;
        Long lastSummary = timeMap.get(key);

        if (lastSummary == null)
            lastSummary = 0L;

        timeMap.put(key, System.nanoTime() - lastTime + lastSummary);
        Long lastCount = timeHappenCount.get(key);

        if (lastCount == null)
            lastCount = 0L;

        timeHappenCount.put(key, ++lastCount);
        lastTime = thisTime;
        lastMark = mark;
    }

    private static final String TPL = "Total times: %5sms, %10sns, Repeat times:%3s, Avg times: %5sms by %25s%n";

    public static void timerPrint() {
        for (Map.Entry<String, Long> entry : timeMap.entrySet()) {
            Long t = entry.getValue();
            Long ms = t / 1_000_000;

            System.out.printf(TPL, ms, t, timeHappenCount.get(entry.getKey()),
                    ms / timeHappenCount.get(entry.getKey()), entry.getKey());
        }
    }
}
