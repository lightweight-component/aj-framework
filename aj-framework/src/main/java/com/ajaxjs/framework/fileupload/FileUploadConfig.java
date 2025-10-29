package com.ajaxjs.framework.fileupload;

import com.ajaxjs.framework.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.framework.fileupload.policy.NamePolicy;
import com.ajaxjs.framework.fileupload.policy.StorageType;
import lombok.Data;

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
    private String baseUploadDir = "c:/temp/uploads";

    /**
     * You can specify a subdirectory under baseUploadDir.
     * It's optional if using `baseUploadDir`.
     * It may not be optional while using S3 storage.
     */
    private String uploadDir;

    /**
     * 允许上传的文件类型，如果为空数组则不限制上传类型。格式如 {".jpg", ".png", ...}
     * The extension name check.
     * If it's null or empty, it means no restriction of ext name.
     */
    private String[] allowExtFilenames;

    /**
     * The type of file to detect.
     */
    private DetectType detectType;

    /**
     * Check file content in Magic Number.
     * If it doesn't meet, try to do the deep check with Apache Tika.
     */
    private boolean checkMagicNumber = true;

    /**
     * The check policy for content-type.
     */
    private ContentTypePolicy.Policy contentTypePolicy = ContentTypePolicy.Policy.ALL;

    /**
     * Name policy
     */
    private NamePolicy.Policy namePolicy = NamePolicy.Policy.ORIGINAL_RANDOM;

    /**
     * The prefix of the URL to access the uploaded file.
     */
    private String urlPrefix;
}
