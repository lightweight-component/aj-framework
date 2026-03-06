package com.ajaxjs.framework.business.video_clip;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/video")
public class VideoController {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/process")
    public ResponseEntity<String> processVideo(@RequestParam MultipartFile file, @RequestParam(required = false) String watermark) {
        try {
            // 生成唯一任务ID
            String taskId = UUID.randomUUID().toString();
            String inputPath = "/uploads/" + taskId + ".mp4";
            // 保存上传文件
            Path path = Paths.get(inputPath);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            // 构建处理命令
            String cmd = String.format("ffmpeg -i %s -c:v libx265 -preset slow %s", inputPath, watermark != null ? "-vf 'movie=watermark.png overlay=10:10'" : "");
            // 异步执行处理
            CompletableFuture.runAsync(() -> {
                FFmpegExecutor.execute(cmd);
                redisTemplate.opsForValue().set("task:" + taskId + ":status", "SUCCESS");
            });
            return ResponseEntity.ok("任务已提交，ID：" + taskId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("处理失败：" + e.getMessage());
        }
    }
}