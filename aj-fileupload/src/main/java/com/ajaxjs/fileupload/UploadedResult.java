package com.ajaxjs.fileupload;

import lombok.Data;

/**
 * File upload result, which can be serialized to the client as a controller return value.
 */
@Data
public class UploadedResult {
    /**
     * File access URL.
     */
    String url;

    /**
     * The actual saved file name; may differ from the original file name depending on the naming policy.
     */
    String fileName;

    /**
     * The original file name provided by the client.
     */
    String originalFileName;

    /**
     * File size, in bytes.
     */
    long fileSize;
}
