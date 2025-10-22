package com.ajaxjs.framework.fileupload;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class FileUploadConfig {
    private StorageType storageType;

    /**
     * Maximum allowed file size in MB
     */
    private long maxFileSize;

    /**
     * Directory path where uploaded files will be saved
     */
    private String uploadDir = "/var/uploads";

    /**
     * 允许上传的文件类型，如果为空数组则不限制上传类型。格式如 {".jpg", ".png", ...}
     */
    private String[] allowExtFilenames;
}
