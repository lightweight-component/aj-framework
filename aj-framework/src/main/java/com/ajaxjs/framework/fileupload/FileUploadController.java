package com.ajaxjs.framework.fileupload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    // 上传目录（application.yml 中也要配置静态资源）
    @Value("${file-upload.path:./uploads/audio/}")
    private String uploadPath;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    /**
     * 文件上传接口
     */
    @PostMapping(value = "/", consumes = "multipart/form-data")
    public Map<String, String> uploadAudio(
            @RequestParam("file") MultipartFile file,  // 必须与 name 一致
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "type", required = false) String type,
            HttpServletRequest request) {

        // 2. 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        File dir = uploadDir.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 3. 生成唯一文件名（避免冲突）
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".mp3";
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        File dest = new File(dir, fileName);

        try {
            // 4. 保存文件
            file.transferTo(dest);

            // 5. 生成可访问的 URL
            // 假设你的域名是 https://yourdomain.com
            String baseUrl = "https://yourdomain.com"; // 改为你的实际域名
            String fileUrl = baseUrl + "/uploads/audio/" + fileName;

            // 返回数据
            Map<String, String> data = new HashMap<>();
            data.put("url", fileUrl);
            data.put("fileName", fileName);
            data.put("size", String.valueOf(file.getSize()));
            data.put("type", type);
            data.put("userId", userId);

            return data;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
