package com.ajaxjs.devtools.jmxmonitor.jvm.model;

import lombok.Data;

import java.util.List;

/**
 * Overview
 */
@Data
public class Overview {
    private SystemInfo systemInfo;

    private JvmInfo jvmInfo;

    private MemoryUsage heapMemoryUsage;

    private MemoryUsage nonHeapMemoryUsage;

    private MemoryUsage metaSpace;

    private ThreadInfo threadInfo;

    private ClassLoadingInfo classLoadingInfo;

    private List<GarbageInfo> garbageCollectorInfo;
}
