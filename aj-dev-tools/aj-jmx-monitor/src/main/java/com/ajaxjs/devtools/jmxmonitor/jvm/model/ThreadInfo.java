package com.ajaxjs.devtools.jmxmonitor.jvm.model;

import lombok.Data;

/**
 * 线程信息
 */
@Data
public class ThreadInfo {
    private int liveThreadCount;

    private int livePeakThreadCount;

    private int daemonThreadCount;

    private long totalStartedThreadCount;
}
