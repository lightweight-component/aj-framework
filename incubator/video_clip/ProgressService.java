package com.ajaxjs.framework.business.video_clip;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ProgressService {
    @Scheduled(fixedRate = 1000)
    public void updateProgress() {
        redisTemplate.keys("task:*:progress").forEach(key -> {
            String taskId = key.split(":")[1];
            String progress = redisTemplate.opsForValue().get(key);
            // 更新数据库状态
            videoRepository.updateProgress(taskId, progress);
            // 达到100%时触发回调
            if ("100%".equals(progress))
                sendCallback(taskId);
        });
    }
}