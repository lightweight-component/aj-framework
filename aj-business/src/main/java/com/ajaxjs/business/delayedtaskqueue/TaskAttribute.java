package com.ajaxjs.business.delayedtaskqueue;

import lombok.Data;

import java.util.Date;

/**
 * 任务属性
 */
@Data
public class TaskAttribute {
    /**
     * 第几个槽位
     */
    private int slotIndex;

    /**
     * 任务应该什么时候执行
     */
    private Date executeTime;

    /**
     * 任务加入槽位的时间
     */
    private Date joinTime;
}
