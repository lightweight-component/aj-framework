package com.ajaxjs.framework.business.video_clip2;

import com.ajaxjs.framework.business.video_clip2.model.ProcessType;
import com.ajaxjs.framework.business.video_clip2.model.VideoProcessTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class VideoProcessService {
    @Autowired
    TaskQueueService taskQueueService;

    @Async("taskExecutor")
    public CompletableFuture<String> processVideo(VideoProcessTask task) {
        try {
            // 更新任务状态为处理中
            taskQueueService.updateTaskStatus(task.getTaskId(), "processing");

            // 执行FFmpeg命令
            String result = executeFFmpegCommand(task);

            // 更新任务状态为完成
            taskQueueService.updateTaskStatus(task.getTaskId(), "completed");
            taskQueueService.updateTaskStatus(task.getTaskId(), "resultUrl", result);

            // 发送回调
            sendCallback(task.getCallbackUrl(), task.getTaskId(), "completed", result);

            return CompletableFuture.completedFuture("success");
        } catch (Exception e) {
            // 更新任务状态为失败
            taskQueueService.updateTaskStatus(task.getTaskId(), "failed");
            return CompletableFuture.completedFuture("failed");
        }
    }

    private String executeFFmpegCommand(VideoProcessTask task) {
        // 构建FFmpeg命令
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(task.getOriginalUrl());

        // 根据处理类型添加参数
        for (ProcessType operation : task.getOperations()) {
            switch (operation) {
                case TRANSCODE:
                    command.add("-c:v");
                    command.add("libx264");
                    command.add("-c:a");
                    command.add("aac");
                    break;
                case SCREENSHOT:
                    command.add("-ss");
                    command.add("00:00:10"); // 截取第10秒的画面
                    command.add("-vframes");
                    command.add("1");
                    break;
                case WATERMARK:
                    command.add("-i");
                    command.add("watermark.png");
                    command.add("-filter_complex");
                    command.add("[1][0]overlay=10:10");
                    break;
            }
        }

        // 输出文件
        String outputPath = generateOutputPath(task.getTaskId());
        command.add(outputPath);

        // 执行命令
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 等待处理完成
        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (exitCode != 0)
            throw new RuntimeException("FFmpeg processing failed");

        return outputPath;
    }

    private void executeFFmpegWithProgress(VideoProcessTask task) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", task.getOriginalUrl());
        Process process = processBuilder.start();

        // 读取FFmpeg的进度信息
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.contains("frame="))
                updateProgress(task.getTaskId(), parseProgress(line));   // 解析进度信息并更新到Redis
        }

        process.waitFor();
    }
}