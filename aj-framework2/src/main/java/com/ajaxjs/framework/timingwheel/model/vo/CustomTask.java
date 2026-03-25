package com.ajaxjs.framework.timingwheel.model.vo;

import lombok.Data;

/**
 * 自定义任务响应类
 */
@Data
public class CustomTask {
    /**
     * 任务 ID
     */
    private String taskId;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 延迟时间（毫秒）
     */
    private long delay;
    
    /**
     * 任务动作
     */
    private String action;
    
    /**
     * 响应消息
     */
    private String message;
}
