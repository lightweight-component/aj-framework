package com.ajaxjs.framework.fileupload.policy;

/**
 * The file storage type.
 */
public enum StorageType {
    /**
     * THe local file system of the server.
     */
    LOCAL_DISK,

    /**
     * Stored in the database.
     */
    DATABASE,

    /**
     * Specific file storage system, like S3, MinIO.
     * It'll upload to that S3 service by SDK or that exposed HTTP endpoint, instead of saving to the local server.
     */
    FILE_SERVICE,

    /**
     * Specific file storage system, like S3, MinIO.
     * The file service exposes an HTTP endpoint for uploading files.
     * So we just need to upload to that endpoint.
     * All we can do maybe just return a 302 redirect to that endpoint.
     */
    FILE_SERVICE_API;
}
