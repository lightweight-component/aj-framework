package com.ajaxjs.framework.timingwheel.model.vo;

import lombok.Data;

/**
 * 压力测试响应类
 */
@Data
public class StressTest {
    /**
     * 任务数量
     */
    private int taskCount;
    
    /**
     * 创建时间（毫秒）
     */
    private long creationTime;
    
    /**
     * 吞吐量（任务/秒）
     */
    private double throughput;
    
    /**
     * 响应消息
     */
    private String message;
}
