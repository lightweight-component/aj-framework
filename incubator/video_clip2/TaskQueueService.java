package com.ajaxjs.framework.business.video_clip2;

import com.ajaxjs.framework.business.video_clip2.model.VideoProcessTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TaskQueueService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 添加任务到队列
    public void addTask(VideoProcessTask task) {
        redisTemplate.opsForList().leftPush("video_process_queue", task);
    }

    // 从队列获取任务
    public VideoProcessTask getTask() {
        return (VideoProcessTask) redisTemplate.opsForList()
                .rightPop("video_process_queue", 30, TimeUnit.SECONDS);
    }

    // 更新任务状态
    public void updateTaskStatus(String taskId, String status) {
        String key = "task:" + taskId;
        redisTemplate.opsForHash().put(key, "status", status);
        redisTemplate.opsForHash().put(key, "updateTime", System.currentTimeMillis());
    }
}