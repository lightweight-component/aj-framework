package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.fileupload.policy.NamePolicy;
import com.ajaxjs.fileupload.policy.StorageType;
import lombok.Data;

/**
 * 文件上传配置。
 * <p>默认使用本地磁盘、10 MB 上限、不限定检测类别，并启用魔数检查。</p>
 */
@Data
public class FileUploadConfig {
    /** 文件存储类型。 */
    private StorageType storageType = StorageType.LOCAL_DISK;

    /**
     * 最大文件大小，单位 MB；必须大于 0，换算为字节时不得溢出 {@code long}。
     */
    private long maxFileSize = 10;

    /**
     * 本地存储基础目录。
     */
    private String baseUploadDir = "c:/temp/uploads";

    /**
     * 基础目录下的可选相对子目录；不得为绝对路径或逃逸基础目录。
     */
    private String uploadDir;

    /**
     * 允许上传的扩展名；{@code null} 或空数组表示不限制。
     * 扩展名不包含前导点，例如 {@code {"jpg", "png"}}。
     */
    private String[] allowExtFilenames;

    /**
     * 文件内容检测类别。
     */
    private DetectType detectType = DetectType.NONE;

    /**
     * 是否执行魔数或容器结构检查。
     */
    private boolean checkMagicNumber = true;

    /**
     * Content-Type 校验策略。
     */
    private ContentTypePolicy.Policy contentTypePolicy = ContentTypePolicy.Policy.ALL;

    /**
     * 保存文件的命名策略。
     */
    private NamePolicy.Policy namePolicy = NamePolicy.Policy.ORIGINAL_RANDOM;

    /**
     * 上传结果中的文件访问 URL 前缀。
     */
    private String urlPrefix = "";
}
