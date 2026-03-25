package com.ajaxjs.framework.timingwheel.model.vo;

import lombok.Data;

/**
 * 取消任务响应类
 */
@Data
public class CancelTask {
    /**
     * 任务 ID
     */
    private String taskId;
    
    /**
     * 是否已取消
     */
    private boolean cancelled;
    
    /**
     * 响应消息
     */
    private String message;
}
