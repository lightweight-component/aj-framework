package com.ajaxjs.devtools.jmxmonitor.tomcat_jmx.model;

import lombok.Data;

@Data
public class JvmInfo {
    /**
     * 堆最大
     */
    private long maxMemory;

    /**
     * 堆当前分配
     */
    private long commitMemory;

    /**
     * 使用内存
     */
    private long usedMemory;

    /**
     * 堆使用率
     */
    private int heap;

    private int nonHeap;

    /**
     *
     */
    private long nonCommitMemory;

    /**
     *
     */
    private long nonUsedMemory;

    /**
     * 持久堆大小
     */
    private long committed;

    /**
     *
     */
    private long used;

    /**
     * 持久堆使用率
     */
    private long permUse;


}
