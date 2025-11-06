package com.ajaxjs.framework.profiler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 代表一个计时单元
 */
public final class Entry {
    //subEntries来表示树状的子节点
    private final List<Entry> subEntries = new ArrayList<>(4);
    private final Object message;
    private final Entry parentEntry;
    private final Entry firstEntry;
    private final long baseTime;
    private final long startTime;
    private long endTime;

    Entry(Object message/*描述信息*/, Entry parentEntry/*父节点信息*/, Entry firstEntry/*第一个节点*/) {
        this.message = message;
        this.startTime = ProfilerSwitch.getInstance().isOpenProfilerNanoTime() ? System.nanoTime() : System.currentTimeMillis();
        this.parentEntry = parentEntry;
        this.firstEntry = (Entry) defaultIfNull(firstEntry, this);
        this.baseTime = (firstEntry == null) ? 0 : firstEntry.startTime;
    }

    /**
     * 取得entry的信息。
     */
    public String getMessage() {
        return defaultIfEmpty((String) message, null);
    }

    public static String defaultIfEmpty(String str, String defaultStr) {
        return ((str == null) || (str.length() == 0)) ? defaultStr : str;
    }

    public static Object defaultIfNull(Object object, Object defaultValue) {
        return (object != null) ? object : defaultValue;
    }

    /**
     * 获取当前节点的开始时间
     */
    public long getStartTime() {
        return (baseTime > 0) ? (startTime - baseTime) : 0;
    }

    /**
     * 获取当前节点的结束时间
     */
    public long getEndTime() {
        return endTime < baseTime ? -1 : endTime - baseTime;
    }

    /**
     * 获取持续时间
     */
    public long getDuration() {
        return endTime < startTime ? -1 : endTime - startTime;
    }

    /**
     * 取得entry自身所用的时间，即总时间减去所有子entry所用的时间。
     */
    public long getDurationOfSelf() {
        long duration = getDuration();

        if (duration < 0)
            return -1;
        else if (subEntries.isEmpty())
            return duration;
        else {
            for (Entry entry : subEntries)
                duration -= entry.getDuration();

            return duration < 0 ? -1 : duration;
        }
    }

    /**
     * 取得当前entry在父entry中所占的时间百分比。
     */
    public double getPecentage() {
        double parentDuration = 0;
        double duration = getDuration();

        if ((parentEntry != null) && parentEntry.isReleased())
            parentDuration = parentEntry.getDuration();

        return (duration > 0) && (parentDuration > 0) ? duration / parentDuration : 0;
    }

    /**
     * 取得当前entry在第一个entry中所占的时间百分比。
     */
    public double getPecentageOfAll() {
        double firstDuration = 0;
        double duration = getDuration();

        if ((firstEntry != null) && firstEntry.isReleased())
            firstDuration = firstEntry.getDuration();

        return (duration > 0) && (firstDuration > 0) ? duration / firstDuration : 0;
    }

    /**
     * 取得所有子entries。
     */
    public List<Entry> getSubEntries() {
        return Collections.unmodifiableList(subEntries);
    }

    /**
     * 结束当前entry，并记录结束时间。
     */
    void release() {
        endTime = ProfilerSwitch.getInstance().isOpenProfilerNanoTime() ? System.nanoTime() : System.currentTimeMillis();
    }

    /**
     * 判断当前entry是否结束。
     */
    public boolean isReleased() {
        return endTime > 0;
    }

    /**
     * 创建一个新的子entry。
     */
    void enterSubEntry(Object message) {
        Entry subEntry = new Entry(message, this, firstEntry);
        subEntries.add(subEntry);
    }

    /**
     * 取得未结束的子entry,链表中的最后一个元素
     */
    Entry getUnreleasedEntry() {
        Entry subEntry = null;

        if (!subEntries.isEmpty()) {
            subEntry = subEntries.get(subEntries.size() - 1);
            if (subEntry.isReleased())
                subEntry = null;
        }

        return subEntry;
    }

    public String toString() {
        return toString("", "");
    }

    String toString(String prefix1, String prefix2) {
        StringBuffer buffer = new StringBuffer();
        toString(buffer, prefix1, prefix2);

        return buffer.toString();
    }

    private void toString(StringBuffer buffer, String prefix1, String prefix2) {
        buffer.append(prefix1);
        String message = getMessage();
        long startTime = getStartTime();
        long duration = getDuration();
        long durationOfSelf = getDurationOfSelf();
        double percent = getPecentage();
        double percentOfAll = getPecentageOfAll();

        Object[] params = new Object[]{
                message, // {0} - entry信息
                startTime, // {1} - 起始时间
                duration, // {2} - 持续总时间
                durationOfSelf, // {3} - 自身消耗的时间
                percent, // {4} - 在父entry中所占的时间比例
                percentOfAll // {5} - 在总时间中所旧的时间比例
        };

        StringBuilder pattern = new StringBuilder("{1,number} ");

        if (isReleased()) {
            pattern.append("[{2,number}");
            pattern.append(ProfilerSwitch.getInstance().isOpenProfilerNanoTime() ? "ns" : "ms");

            if ((durationOfSelf > 0) && (durationOfSelf != duration)) {
                pattern.append(" ({3,number})");
                pattern.append(ProfilerSwitch.getInstance().isOpenProfilerNanoTime() ? "ns" : "ms");
            }

            if (percent > 0)
                pattern.append(", {4,number,##%}");

            if (percentOfAll > 0)
                pattern.append(", {5,number,##%}");

            pattern.append("]");
        } else
            pattern.append("[UNRELEASED]");

        if (message != null)
            pattern.append(" - {0}");

        buffer.append(MessageFormat.format(pattern.toString(), params));

        for (int i = 0; i < subEntries.size(); i++) {
            Entry subEntry = subEntries.get(i);
            buffer.append('\n');

            if (i == (subEntries.size() - 1))
                subEntry.toString(buffer, prefix2 + "`---", prefix2 + "    "); // 最后一项
            else if (i == 0)
                subEntry.toString(buffer, prefix2 + "+---", prefix2 + "|   "); // 第一项
            else
                subEntry.toString(buffer, prefix2 + "+---", prefix2 + "|   "); // 中间项
        }
    }
}