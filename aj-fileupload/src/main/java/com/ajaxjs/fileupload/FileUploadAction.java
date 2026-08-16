package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.fileupload.policy.NamePolicy;
import com.ajaxjs.fileupload.policy.StorageType;

import java.lang.annotation.*;

/**
 * 声明控制器方法的文件上传策略。
 * <p>{@link UploadUtils} 会读取该注解并构造 {@link FileUploadConfig}。</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FileUploadAction {
    /**
     * 文件存储类型。
     *
     * @return 存储类型
     */
    StorageType storageType() default StorageType.LOCAL_DISK;

    /**
     * 允许的最大文件大小，单位 MB；必须大于 0。
     *
     * @return 最大文件大小（MB）
     */
    long maxFileSize() default 10;

    /**
     * 本地文件存储的基础目录。
     *
     * @return 基础目录路径
     */
    String baseUploadDir() default "c:/temp/uploads";

    /**
     * 基础目录下的可选相对子目录，不得逃逸基础目录。
     *
     * @return 相对子目录，空字符串表示不使用子目录
     */
    String uploadDir() default "";

    /**
     * 允许的文件扩展名，例如 {@code {"jpg", "png"}}；空数组表示不限制。
     * 扩展名不包含前导点。
     *
     * @return 允许的扩展名数组
     */
    String[] allowExtFilenames() default {};

    /**
     * 文件内容检测类别。
     *
     * @return 检测类别
     */
    DetectType detectType() default DetectType.NONE;

    /**
     * 是否根据文件头或容器结构执行魔数检查。
     * <p>当前实现不会回退到 Apache Tika。</p>
     *
     * @return 启用返回 {@code true}
     */
    boolean checkMagicNumber() default true;

    /**
     * Content-Type 校验策略。
     *
     * @return Content-Type 校验策略
     */
    ContentTypePolicy.Policy contentTypePolicy() default ContentTypePolicy.Policy.ALL;

    /**
     * 保存文件的命名策略。
     *
     * @return 命名策略
     */
    NamePolicy.Policy namePolicy() default NamePolicy.Policy.ORIGINAL_RANDOM;

    /**
     * 上传完成后返回的文件访问 URL 前缀。
     *
     * @return URL 前缀
     */
    String urlPrefix() default "";
}
