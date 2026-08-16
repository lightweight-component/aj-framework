package com.ajaxjs.fileupload.policy;

import com.ajaxjs.util.RandomTools;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文件的安全命名策略。
 * <p>所有策略都会先移除客户端文件名中的路径部分并拒绝控制字符。</p>
 */
public class NamePolicy {
    /**
     * 可用的上传文件命名方式。
     */
    public enum Policy {
        /**
         * 使用清理后的原始文件名；可能发生同名冲突。
         */
        ORIGINAL,

        /**
         * 在原始基本名称后追加 UUID，并保留扩展名。
         */
        ORIGINAL_RANDOM,

        /**
         * 仅使用 UUID 和原始扩展名。
         */
        RANDOM
    }

    final String fileName;

    final Policy policy;

    /**
     * 创建文件名策略。
     *
     * @param fileName 客户端文件名；可以包含浏览器传入的路径部分
     * @param policy   命名策略
     */
    public NamePolicy(String fileName, Policy policy) {
        this.fileName = fileName;
        this.policy = policy;
    }

    /**
     * 从 multipart 文件创建命名策略。
     *
     * @param file   multipart 文件
     * @param policy 命名策略
     */
    public NamePolicy(MultipartFile file, Policy policy) {
        this(file.getOriginalFilename(), policy);
    }

    /**
     * 根据策略生成安全文件名。
     *
     * @return 不包含目录部分的文件名
     * @throws IllegalArgumentException 原始文件名或策略不合法时抛出
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
     * 移除客户端提供的 Unix/Windows 路径并校验剩余文件名。
     *
     * @param originalFilename 客户端提供的原始文件名
     * @return 不含路径部分的文件名
     * @throws IllegalArgumentException 名称为 {@code null}、空、点目录或含控制字符时抛出
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
     * 返回最后一个点之前的基本名称。
     *
     * @param fileName 文件名
     * @return 基本名称；没有点时返回原值
     */
    public static String getBaseName(String fileName) {
        int lastDot = fileName.lastIndexOf('.');

        return (lastDot == -1) ? fileName : fileName.substring(0, lastDot);
    }

    /**
     * 从 multipart 文件的原始名称取得扩展名。
     *
     * @param file multipart 文件
     * @return 不含点的扩展名
     */
    public static String getFileExtension(MultipartFile file) {
        return getFileExtension(file.getOriginalFilename());
    }

    /**
     * 取得最后一个点之后的扩展名。
     *
     * @param fileName 文件名
     * @return 不含点的扩展名
     * @throws IllegalArgumentException 名称为 {@code null} 或不含点时抛出
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains("."))
            throw new IllegalArgumentException("The file uploaded doesn't hava a extension name.");

        int lastDot = fileName.lastIndexOf('.');

        return fileName.substring(lastDot + 1);
    }

    /**
     * 在原始基本名称后追加 UUID。
     *
     * @param fileName 已清理的文件名
     * @return {@code 基本名称_UUID.扩展名}
     */
    public static String nameOriginalRandom(String fileName) {
        return getBaseName(fileName).trim() + "_" + RandomTools.uuidStr() + "." + getFileExtension(fileName);
    }
}
