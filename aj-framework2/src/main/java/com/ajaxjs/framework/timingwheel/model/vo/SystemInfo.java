package com.ajaxjs.framework.timingwheel.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统信息响应类
 */
@Data
public class SystemInfo {
    /**
     * 可用处理器数量
     */
    private int availableProcessors;
    
    /**
     * 空闲内存（字节）
     */
    private long freeMemory;
    
    /**
     * 总内存（字节）
     */
    private long totalMemory;
    
    /**
     * 最大内存（字节）
     */
    private long maxMemory;
    
    /**
     * 使用内存（字节）
     */
    private long usedMemory;
    
    /**
     * 当前时间
     */
    private LocalDateTime currentTime;
}
