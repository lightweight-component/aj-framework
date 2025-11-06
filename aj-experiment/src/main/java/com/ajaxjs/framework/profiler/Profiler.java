package com.ajaxjs.framework.profiler;

import java.util.LinkedList;
import java.util.List;

/**
 * 用来测试并统计线程执行时间的工具
 */
public final class Profiler {
    /**
     * 构建实体的存储缓存体
     */
    private static final ThreadLocal<Entry> entryStack = new ThreadLocal<>();

    /**
     * 开始计时
     */
    public static void start() {
        start(null);
    }

    /**
     * 开始计时，创建一个Entry的实体对象
     */
    public static void start(String message) {
        entryStack.set(new Entry(message, null, null));
    }

    /**
     * threadLocal缓存清理，由于现在大多是线程池的设置，所以要做一个清理
     */
    public static void reset() {
        entryStack.set(null);
    }

    /**
     * 由于Entry自身是树状结构，所以如果是进入非Root的节点，那就需要enter来搞
     */
    public static void enter(String message) {
        Entry currentEntry = getCurrentEntry();

        if (currentEntry != null)
            currentEntry.enterSubEntry(message);
    }

    /**
     * 方法运行结束之后，把当前的Entry的endTime来设置成当前时间
     */
    public static void release() {
        Entry currentEntry = getCurrentEntry();

        if (currentEntry != null)
            currentEntry.release();
    }

    /**
     * 获取start和end的时间差
     */
    public static long getDuration() {
        Entry entry = entryStack.get();

        return entry == null ? -1 : entry.getDuration();
    }

    /**
     * 把Entry的信息dump出来，可以打印到日志中去
     */
    public static String dump() {
        return dump("", "");
    }

    public static String dump(String prefix1, String prefix2) {
        Entry entry = entryStack.get();

        return entry != null ? entry.toString(prefix1, prefix2) : "";
    }

    /**
     * 获取Entry信息
     */
    public static Entry getEntry() {
        return entryStack.get();
    }

    /**
     * entry中含有subentry，如此这样来进行循环来保持树状的结构
     */
    private static Entry getCurrentEntry() {
        Entry subEntry = entryStack.get();
        Entry entry = null;

        if (subEntry != null) {
            do {
                entry = subEntry;
                subEntry = entry.getUnreleasedEntry();
            } while (subEntry != null);
        }

        return entry;
    }


    public static String[] split(String str, String separatorChars) {
        return split(str, separatorChars, -1);
    }

    private static String[] split(String str, String separatorChars, int max) {
        if (str == null)
            return null;

        int length = str.length();

        if (length == 0)
            return new String[0];

        List<String> list = new LinkedList<>();
        int sizePlus1 = 1;
        int i = 0;
        int start = 0;
        boolean match = false;

        if (separatorChars == null) {
            // null表示使用空白作为分隔符
            while (i < length) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match) {
                        if (sizePlus1++ == max)
                            i = length;

                        list.add(str.substring(start, i));
                        match = false;
                    }

                    start = ++i;
                    continue;
                }

                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // 优化分隔符长度为1的情形
            char sep = separatorChars.charAt(0);

            while (i < length) {
                if (str.charAt(i) == sep) {
                    if (match) {
                        if (sizePlus1++ == max)
                            i = length;

                        list.add(str.substring(start, i));
                        match = false;
                    }

                    start = ++i;
                    continue;
                }

                match = true;
                i++;
            }
        } else {
            // 一般情形
            while (i < length) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match) {
                        if (sizePlus1++ == max)
                            i = length;

                        list.add(str.substring(start, i));
                        match = false;
                    }

                    start = ++i;
                    continue;
                }

                match = true;
                i++;
            }
        }

        if (match)
            list.add(str.substring(start, i));

        return list.toArray(new String[list.size()]);
    }
}
