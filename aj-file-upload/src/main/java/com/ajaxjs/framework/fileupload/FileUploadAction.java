package com.ajaxjs.framework.fileupload;


import com.ajaxjs.framework.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.framework.fileupload.policy.NamePolicy;
import com.ajaxjs.framework.fileupload.policy.StorageType;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileUploadAction {
    /**
     * Storage type for uploaded files (e.g., LOCAL, CLOUD).
     */
    StorageType storageType() default StorageType.LOCAL_DISK;

    /**
     * Maximum allowed file size in megabytes.
     */
    long maxFileSize() default 10;

    /**
     * Base directory where uploaded files will be saved.
     */
    String baseUploadDir() default "c:/temp/uploads";

    /**
     * Optional subdirectory under baseUploadDir.
     */
    String uploadDir() default "";

    /**
     * Allowed file extensions (e.g., {".jpg", ".png"}). Empty means no restriction.
     */
    String[] allowExtFilenames() default {};

    /**
     * File type detection method (e.g., image).
     */
    DetectType detectType() default DetectType.NONE;

    /**
     * Check file content in Magic Number.
     * If it doesn't meet, try to do the deep check with Apache Tika.
     */
    boolean checkMagicNumber() default true;

    /**
     * The check policy for content-type.
     */
    ContentTypePolicy.Policy contentTypePolicy() default ContentTypePolicy.Policy.ALL;

    /**
     * Naming policy for uploaded files (e.g., original name, timestamp, random).
     */
    NamePolicy.Policy namePolicy() default NamePolicy.Policy.ORIGINAL_RANDOM;

    /**
     * URL prefix used to access uploaded files.
     */
    String urlPrefix() default "";
}