package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.fileupload.policy.NamePolicy;
import com.ajaxjs.fileupload.policy.StorageType;

import java.lang.annotation.*;

/**
 * Declares the file upload policy for a controller method.
 * <p>{@link UploadUtils} reads this annotation and constructs {@link FileUploadConfig}.</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileUploadAction {
    /**
     * File storage type.
     *
     * @return storage type
     */
    StorageType storageType() default StorageType.LOCAL_DISK;

    /**
     * The Maximum allowed file size, in MB, must be greater than 0.
     *
     * @return maximum file size (MB)
     */
    long maxFileSize() default 10;

    /**
     * Base directory for local file storage.
     *
     * @return base directory path
     */
    String baseUploadDir() default "c:/temp/uploads";

    /**
     * Optional relative subdirectory under the base directory; must not escape the base directory.
     *
     * @return relative subdirectory, empty string means no subdirectory is used
     */
    String uploadDir() default "";

    /**
     * Allowed file extensions, e.g., {@code {"jpg", "png"}}; an empty array means no restriction.
     * Extensions do not include the leading dot.
     *
     * @return array of allowed extensions
     */
    String[] allowExtFilenames() default {};

    /**
     * File content detection category.
     *
     * @return detection category
     */
    DetectType detectType() default DetectType.NONE;

    /**
     * Whether to perform magic number checking based on file headers or container structure.
     * <p>The current implementation does not fall back to Apache Tika.</p>
     *
     * @return {@code true} if enabled
     */
    boolean checkMagicNumber() default true;

    /**
     * Content-Type validation policy.
     *
     * @return Content-Type validation policy
     */
    ContentTypePolicy.Policy contentTypePolicy() default ContentTypePolicy.Policy.ALL;

    /**
     * Naming policy for saved files.
     *
     * @return naming policy
     */
    NamePolicy.Policy namePolicy() default NamePolicy.Policy.ORIGINAL_RANDOM;

    /**
     * URL prefix for file access returned after upload completes.
     *
     * @return URL prefix
     */
    String urlPrefix() default "";
}
