package com.ajaxjs.fileupload;

/**
 * Content detection type for uploaded files.
 * <p>This type is used to select extension, Content-Type, and magic number validation rules; it does not indicate that the file has been fully parsed.</p>
 */
public enum DetectType {
    /**
     * No file type restriction.
     */
    NONE,

    /**
     * Image files.
     */
    IMAGE,

    /**
     * Documents such as Office files, PDFs, and text files.
     */
    OFFICE_FILE,

    /**
     * Audio files.
     */
    AUDIO,

    /**
     * Video files.
     */
    VIDEO
}
