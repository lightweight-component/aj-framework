package com.ajaxjs.framework.fileupload.policy;

import com.ajaxjs.util.RandomTools;
import org.springframework.web.multipart.MultipartFile;

/**
 * How to name the uploaded file.
 */
public class NamePolicy {
    public enum Policy {
        /**
         * Use the original file name.
         * It's easy to conflict when uploading multiple files.
         */
        ORIGINAL,

        /**
         * Use a random name that appends after the original.
         */
        ORIGINAL_RANDOM,

        /**
         * Use a random name with the original file extension.
         */
        RANDOM
    }

    final String fileName;

    final Policy policy;

    public NamePolicy(String fileName, Policy policy) {
        this.fileName = fileName;
        this.policy = policy;
    }

    public NamePolicy(MultipartFile file, Policy policy) {
        this(file.getOriginalFilename(), policy);
    }

    public String getFileName() {
        switch (policy) {
            case ORIGINAL:
                return fileName;
            case ORIGINAL_RANDOM:
                return nameOriginalRandom(fileName);
            case RANDOM:
                return RandomTools.uuidStr() + "." + getFileExtension(fileName);
            default:
                throw new IllegalArgumentException("Invalid policy: " + policy);
        }
    }

    public static String getBaseName(String fileName) {
        int lastDot = fileName.lastIndexOf('.');

        return (lastDot == -1) ? fileName : fileName.substring(0, lastDot);
    }

    public static String getFileExtension(MultipartFile file) {
        return getFileExtension(file.getOriginalFilename());
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains("."))
            throw new IllegalArgumentException("The file uploaded doesn't hava a extension name.");

        int lastDot = fileName.lastIndexOf('.');

        return fileName.substring(lastDot + 1);
    }

    public static String nameOriginalRandom(String fileName) {
        return getBaseName(fileName) + "_" + RandomTools.uuidStr() + "." + getFileExtension(fileName);
    }
}
