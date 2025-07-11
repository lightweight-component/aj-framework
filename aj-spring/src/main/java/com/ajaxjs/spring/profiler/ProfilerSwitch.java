package com.ajaxjs.spring.profiler;

import lombok.Data;

@Data
public class ProfilerSwitch {
    private static ProfilerSwitch instance = new ProfilerSwitch();

    public static ProfilerSwitch getInstance() {
        return instance;
    }

    /**
     * 是否打开打印日志的开关
     */
    private boolean openProfilerTree = false;

    /**
     * 超时时间
     */
    private long invokeTimeout = 500;

    /**
     * 是否打印纳秒
     */
    private boolean openProfilerNanoTime = false;
}