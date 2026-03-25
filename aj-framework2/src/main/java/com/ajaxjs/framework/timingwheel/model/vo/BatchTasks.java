package com.ajaxjs.framework.timingwheel.model.vo;

import lombok.Data;
import java.util.List;

/**
 * 批量任务响应类
 */
@Data
public class BatchTasks {
    /**
     * 任务 ID 列表
     */
    private List<String> taskIds;
    
    /**
     * 任务数量
     */
    private int count;
    
    /**
     * 响应消息
     */
    private String message;
}
