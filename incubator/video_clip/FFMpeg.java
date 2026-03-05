package com.ajaxjs.framework.business.video_clip;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FFMpeg {

    // NVIDIA CUDA加速配置示例
    public static String getCudaCommand(String input, String output) {
        return String.format("ffmpeg -hwaccel cuda -i %s -c:v h264_nvenc -preset fast -b:v 5M -c:a aac %s", input, output);
    }

    // 硬件检测工具类
    public static boolean isCudaSupported() {
        String result = FFmpegExecutor.execute("ffmpeg -encoders | grep nvenc");

        return result.contains("cuvid") && result.contains("nvcuvid");
    }

    // 多步骤处理示例：转码+水印+截图
    public void processVideo(String input, String output) {
        // 第一步：转码
        String tempOutput = "/tmp/" + UUID.randomUUID() + ".mp4";
        executeCommand(getTranscodeCommand(input, tempOutput));
        // 第二步：添加水印
        executeCommand(String.format("ffmpeg -i %s -i watermark.png " + "-filter_complex overlay=10:10 %s", tempOutput, output));
        // 第三步：生成截图
        executeCommand(String.format("ffmpeg -i %s -ss 00:00:05 -vframes 1 %s", output, output.replace(".mp4", ".jpg")));
    }

    // 内存控制配置
    public static void setMemoryLimit() {
        // 设置FFmpeg进程内存限制(单位：MB)
        System.setProperty("ffmpeg.mem_limit", "2048");
        // JVM参数优化    -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
    }

    // 线程池配置
    @Bean
    public ExecutorService ffmpegThreadPool() {
        return new ThreadPoolExecutor(10, 50, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    // Redis队列高级用法
    public class RedisQueue {
        @Autowired
        private RedisTemplate<String, String> template;

        // 优先级队列
        public void enqueue(String taskId, int priority) {
            template.opsForList().rightPush("video:queue:" + priority, taskId);
        }

        // 延迟队列
        public void scheduleTask(String taskId, long delay) {
            template.opsForZSet().add("video:schedule", taskId, System.currentTimeMillis() + delay);
        }
    }
}
