package com.ajaxjs.devtools.sysmonitor.model;
import lombok.Data;

/**
 * 系统内存信息
 */
@Data
public class MemoryInfo {
    /**
     * 总计
     */
    private String total;

    /**
     * 已使用
     */
    private String used;

    /**
     * 未使用
     */
    private String free;

    /**
     * 使用率
     */
    private double usePercent;
}
