package com.ajaxjs.framework.timingwheel.model.vo;

import lombok.Data;

/**
 * 清理任务响应类
 */
@Data
public class CleanupTasks {
    /**
     * 移除的任务数量
     */
    private int removedCount;
}
