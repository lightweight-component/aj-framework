package com.ajaxjs.framework.timingwheel.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class SlotInfo {
    private int index;
    private int taskCount;
    private long lastAccessTime;
    private Map<TimerTaskWrapper.TaskStatus, Long> statusCounts;
}