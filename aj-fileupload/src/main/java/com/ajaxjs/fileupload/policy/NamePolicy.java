package com.ajaxjs.fileupload.policy;

import com.ajaxjs.util.RandomTools;
import org.springframework.web.multipart.MultipartFile;

/**
 * Secure naming policy for uploaded files.
 * <p>All policies first strip path components from the client filename and reject control characters.</p>
 */
public class NamePolicy {
    /**
     * Available upload file naming modes.
     */
    public enum Policy {
        /**
         * Uses the sanitized original filename; may cause name collisions.
         */
        ORIGINAL,

        /**
         * Appends a UUID after the original base name while preserving the extension.
         */
        ORIGINAL_RANDOM,

        /**
         * Uses only a UUID with the original extension.
         */
        RANDOM
    }

    /**
     * The original client-provided filename.
     */
    final String fileName;

    /**
     * The naming policy to apply.
     */
    final Policy policy;

    /**
     * Creates a filename policy.
     *
     * @param fileName client filename; may contain path components from the browser
     * @param policy   naming policy
     */
    public NamePolicy(String fileName, Policy policy) {
        this.fileName = fileName;
        this.policy = policy;
    }

    /**
     * Creates a naming policy from a multipart file.
     *
     * @param file   multipart file
     * @param policy naming policy
     */
    public NamePolicy(MultipartFile file, Policy policy) {
        this(file.getOriginalFilename(), policy);
    }

    /**
     * Generates a safe filename according to the policy.
     *
     * @return filename without directory components
     * @throws IllegalArgumentException thrown when the original filename or policy is invalid
     */
    public String getFileName() {
        String safeFileName = sanitizeFileName(fileName);

        switch (policy) {
            case ORIGINAL:
                return safeFileName;
            case ORIGINAL_RANDOM:
                return nameOriginalRandom(safeFileName);
            case RANDOM:
                return RandomTools.uuidStr() + "." + getFileExtension(safeFileName);
            default:
                throw new IllegalArgumentException("Invalid policy: " + policy);
        }
    }

    /**
     * Strips Unix/Windows paths from the client-provided filename and validates the remainder.
     *
     * @param originalFilename original filename provided by the client
     * @return filename without path components
     * @throws IllegalArgumentException thrown when the name is {@code null}, empty, a dot directory, or contains control characters
     */
    public static String sanitizeFileName(String originalFilename) {
        if (originalFilename == null)
            throw new IllegalArgumentException("The original filename is required.");

        for (int i = 0; i < originalFilename.length(); i++)
            if (Character.isISOControl(originalFilename.charAt(i)))
                throw new IllegalArgumentException("The original filename contains control characters.");

        String normalized = originalFilename.trim().replace('\\', '/');
        int lastSeparator = normalized.lastIndexOf('/');

        if (lastSeparator >= 0)
            normalized = normalized.substring(lastSeparator + 1);

        if (normalized.isEmpty() || ".".equals(normalized) || "..".equals(normalized))
            throw new IllegalArgumentException("The original filename is invalid.");

        return normalized;
    }

    /**
     * Returns the base name before the last dot.
     *
     * @param fileName filename
     * @return base name; returns the original value if no dot is present
     */
    public static String getBaseName(String fileName) {
        int lastDot = fileName.lastIndexOf('.');

        return (lastDot == -1) ? fileName : fileName.substring(0, lastDot);
    }

    /**
     * Gets the extension from the multipart file's original name.
     *
     * @param file multipart file
     * @return extension without the dot
     */
    public static String getFileExtension(MultipartFile file) {
        return getFileExtension(file.getOriginalFilename());
    }

    /**
     * Gets the extension after the last dot.
     *
     * @param fileName filename
     * @return extension without the dot
     * @throws IllegalArgumentException thrown when the name is {@code null} or has no dot
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains("."))
            throw new IllegalArgumentException("The file uploaded doesn't hava a extension name.");

        int lastDot = fileName.lastIndexOf('.');

        return fileName.substring(lastDot + 1);
    }

    /**
     * Appends a UUID after the original base name.
     *
     * @param fileName sanitized filename
     * @return {@code baseName_UUID.extension}
     */
    public static String nameOriginalRandom(String fileName) {
        return getBaseName(fileName).trim() + "_" + RandomTools.uuidStr() + "." + getFileExtension(fileName);
    }
}
