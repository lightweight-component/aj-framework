package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.policy.NamePolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 音频上传控制器示例。
 * <p>该类没有声明 {@code @RestController}，不会自动注册为 Spring MVC
 * 控制器；它也没有使用完整的 {@link FileUpload} 校验流程，不应直接作为生产
 * 上传端点。</p>
 */
//@RestController
//@RequestMapping("/api/upload")
public class FileUploadController {
    // 上传目录（application.yml 中也要配置静态资源）
    @Value("${file-upload.path:./uploads/audio/}")
    private String uploadPath;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    /**
     * 将音频文件保存到示例目录并返回上传结果。
     *
     * @param file multipart 文件
     * @return 上传结果
     * @throws UncheckedIOException 保存文件失败时抛出
     */
    @PostMapping(value = "/", consumes = "multipart/form-data")
    public UploadedResult uploadAudio(@RequestParam("file") MultipartFile file) {
        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        File dir = uploadDir.toFile();

        if (!dir.exists())
            dir.mkdirs();


        String originalFilename = file.getOriginalFilename();
        String fileName = new NamePolicy(file, NamePolicy.Policy.RANDOM).getFileName();
        File dest = new File(dir, fileName);
        try {
            file.transferTo(new File(dir, fileName));// 保存文件
        } catch (IOException e) {
            throw new UncheckedIOException("Error occurred when saving file to " + dest, e);
        }

        String baseUrl = "https://yourdomain.com"; // 改为你的实际域名
        String fileUrl = baseUrl + "/uploads/audio/" + fileName;

        // 返回数据
        UploadedResult result = new UploadedResult();
        result.setUrl(fileUrl);
        result.setFileName(fileName);
        result.setOriginalFileName(originalFilename);
        result.setFileSize(file.getSize());

        return result;
    }
}
