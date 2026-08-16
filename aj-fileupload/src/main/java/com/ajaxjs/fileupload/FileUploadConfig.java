package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.fileupload.policy.NamePolicy;
import com.ajaxjs.fileupload.policy.StorageType;
import lombok.Data;

/**
 * File upload configuration.
 * <p>Defaults to local disk storage, 10 MB limit, no detection type restriction, and magic number check enabled.</p>
 */
@Data
public class FileUploadConfig {
    /**
     * File storage type.
     */
    private StorageType storageType = StorageType.LOCAL_DISK;

    /**
     * Maximum file size in MB; must be greater than 0 and must not overflow {@code long} when converted to bytes.
     */
    private long maxFileSize = 10;

    /**
     * Local storage base directory.
     */
    private String baseUploadDir = "c:/temp/uploads";

    /**
     * Optional relative subdirectory under the base directory;
     * must not be an absolute path or escape the base directory.
     */
    private String uploadDir;

    /**
     * Allowed file extensions; {@code null} or an empty array means no restriction.
     * Extensions do not include a leading dot, e.g. {@code {"jpg", "png"}}.
     */
    private String[] allowExtFilenames;

    /**
     * File content detection type.
     */
    private DetectType detectType = DetectType.NONE;

    /**
     * Whether to perform magic number or container structure checks.
     */
    private boolean checkMagicNumber = true;

    /**
     * Content-Type validation policy.
     */
    private ContentTypePolicy.Policy contentTypePolicy = ContentTypePolicy.Policy.ALL;

    /**
     * File naming policy for saved files.
     */
    private NamePolicy.Policy namePolicy = NamePolicy.Policy.ORIGINAL_RANDOM;

    /**
     * URL prefix for file access in the upload result.
     */
    private String urlPrefix = "";
}
