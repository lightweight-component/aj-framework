package com.ajaxjs.framework.business.video_clip2.model;

public enum TaskPriority {
    LOW(1),      // 普通任务
    MEDIUM(2),   // 中等优先级
    HIGH(3);     // 高优先级

    private final int value;

    TaskPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}