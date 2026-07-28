package com.ajaxjs.framework.timingwheel.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 时间轮统计信息
 */
@Data
@AllArgsConstructor
public class TimingWheelStats {
    private int slotSize;
    private long tickDuration;
    private int currentSlot;
    private long totalTasks;
    private long completedTasks;
    private long failedTasks;
    private int activeTaskCount;

    private List<SlotInfo> slotInfos;
}