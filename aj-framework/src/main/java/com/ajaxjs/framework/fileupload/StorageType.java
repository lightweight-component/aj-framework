package com.ajaxjs.framework.fileupload;

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
     */
    FILE_SYSTEM;
}
