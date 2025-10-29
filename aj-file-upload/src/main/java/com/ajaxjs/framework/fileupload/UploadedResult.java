package com.ajaxjs.framework.fileupload;

import lombok.Data;

/**
 * The result of file upload that returns to the client.
 */
@Data
public class UploadedResult {
    /**
     * The URL of the uploaded file.
     */
    String url;

    /**
     * The name of the uploaded file. Maybe different from the original file name due to the naming policy.
     */
    String fileName;

    /**
     * The original file name.
     */
    String originalFileName;

    /**
     * The size of the uploaded file.
     */
    long fileSize;
}
