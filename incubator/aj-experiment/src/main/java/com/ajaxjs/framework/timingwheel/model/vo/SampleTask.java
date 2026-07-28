package com.ajaxjs.framework.timingwheel.model.vo;

import lombok.Data;

/**
 * 示例任务响应类
 */
@Data
public class SampleTask {
    /**
     * 任务 ID
     */
    private String taskId;
    
    /**
     * 任务类型
     */
    private String type;
    
    /**
     * 延迟时间（毫秒）
     */
    private long delay;
}
