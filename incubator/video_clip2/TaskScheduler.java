package com.ajaxjs.framework.business.video_clip2;

import com.ajaxjs.framework.business.video_clip2.model.VideoProcessTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskScheduler {

    @Autowired
    private TaskQueueService taskQueueService;

    @Autowired
    private VideoProcessService videoProcessService;

    @Scheduled(fixedRate = 1000) // 每秒检查一次
    public void scheduleTasks() {
        // 检查是否有可用的处理线程
        if (isProcessorAvailable()) {
            VideoProcessTask task = taskQueueService.getTask();

            if (task != null)
                videoProcessService.processVideo(task);
        }
    }

    private boolean isProcessorAvailable() {
        // 检查当前正在处理的任务数量
        // 避免过多并发导致服务器负载过高
        return getCurrentProcessingCount() < MAX_CONCURRENT_TASKS;
    }
}