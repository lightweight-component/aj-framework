package com.ajaxjs.devtools.jmxmonitor.jvm.model;

import lombok.Data;

/**
 * 垃圾回收信息
 */
@Data
public class GarbageInfo {
    private String name;

    private long collectionCount;

    private String[] memoryPoolNames;
}
