package com.ajaxjs.framework.timingwheel.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务执行统计信息
 */
@Data
@AllArgsConstructor
public  class TaskExecutionStats {
    private int totalTasks;
    private long pendingTasks;
    private long runningTasks;
    private long completedTasks;
    private long failedTasks;
    private long cancelledTasks;
    private LocalDateTime timestamp;
}