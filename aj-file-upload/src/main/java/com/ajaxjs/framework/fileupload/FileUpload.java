package com.ajaxjs.framework.fileupload;

import com.ajaxjs.framework.fileupload.magicnumber.MagicNumber;
import com.ajaxjs.framework.fileupload.permission.PermissionCheck;
import com.ajaxjs.framework.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.framework.fileupload.policy.ExtensionCheck;
import com.ajaxjs.framework.fileupload.policy.NamePolicy;
import com.ajaxjs.framework.fileupload.policy.ShowUrlPolicy;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.StrUtil;
import com.ajaxjs.util.UrlHelper;
import com.ajaxjs.util.io.FileHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.function.BiFunction;

@Slf4j
@Data
public class FileUpload {
    final MultipartFile file;

    final FileUploadConfig config;

    public FileUpload(MultipartFile file, FileUploadConfig config) {
        this.file = file;
        this.config = config;
    }

    public UploadedResult save() {
        check();

        switch (config.getStorageType()) {
            case LOCAL_DISK:
                return saveToDisk();
            case DATABASE:
                return saveToDatabase.apply(file, config);
            case FILE_SERVICE:
            case FILE_SERVICE_API:
                return saveToFileService();
            default:
                throw new UnsupportedOperationException("The storage type is not supported.");
        }
    }

    public void check() {
        if (file.isEmpty())
            throw new IllegalArgumentException("没有上传任何文件");

        if (file.getSize() > (config.getMaxFileSize() * 1024 * 1024)) // MB 转字节
            throw new IllegalArgumentException("文件大小超过系统限制！");

        String ext = NamePolicy.getFileExtension(file);

        ExtensionCheck.checkExtName(config, ext);

        if (config.getContentTypePolicy() != ContentTypePolicy.Policy.NO_CHECK)
            new ContentTypePolicy(file, config).check();

        MagicNumber.checkMagicNumber(file, config, ext);
    }

    private UploadedResult saveToDisk() {
        String dir = initDir();
        String fileName = new NamePolicy(file, config.getNamePolicy()).getFileName();
        File dest = new File(dir, fileName);

        try {
            file.transferTo(new File(dir, fileName));// 保存文件
            log.info("File saved to: " + dest);
        } catch (IOException e) {
            log.error("Error occurred when saving file to " + dest, e);
            throw new UncheckedIOException("Error occurred when saving file to " + dest, e);
        }

        String fileUrl = ShowUrlPolicy.concatTwoUrl(config.getUrlPrefix(), fileName);
        // 返回数据
        UploadedResult result = new UploadedResult();
        result.setUrl(fileUrl);
        result.setFileName(fileName);
        result.setOriginalFileName(file.getOriginalFilename());
        result.setFileSize(file.getSize());

        return result;
    }

    private String initDir() {
        String dir = config.getBaseUploadDir();

        if (ObjectHelper.isEmptyText(dir))
            throw new UnsupportedOperationException("The config of upload dir is not given.");

        String subDir = config.getUploadDir();

        if (ObjectHelper.hasText(subDir))
            dir = Paths.get(dir, subDir).toString();

        new FileHelper(dir).createDirectory(); // 创建上传目录

        PermissionCheck.check(dir);

        return dir;
    }

    /**
     * Set the URL prefix of the uploaded file.
     *
     * @param baseUrl     Domain URL, like <a href="https://yourdomain.com">...</a>
     * @param contextPath The context path of the application, like /your_app
     * @param uploadPath  The upload path, like /uploads
     */
    public void setUrlPrefix(String baseUrl, String contextPath, String uploadPath) {
        String urlPrefix = UrlHelper.concatUrl(baseUrl, contextPath);
        urlPrefix = UrlHelper.concatUrl(urlPrefix, uploadPath);

        config.setUrlPrefix(urlPrefix);
    }

    BiFunction<MultipartFile, FileUploadConfig, UploadedResult> saveToDatabase;

    private UploadedResult saveToFileService() {
        UploadedResult result = new UploadedResult();
        return result;
    }
}
