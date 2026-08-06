package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.magicnumber.MagicNumber;
import com.ajaxjs.fileupload.permission.PermissionCheck;
import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.fileupload.policy.ExtensionCheck;
import com.ajaxjs.fileupload.policy.NamePolicy;
import com.ajaxjs.fileupload.policy.ShowUrlPolicy;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.UrlHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;

/**
 * 单个 {@link MultipartFile} 的校验与保存处理器。
 * <p>实例绑定一个上传文件和一份配置，不应在多个上传请求之间复用。</p>
 */
@Slf4j
@Data
public class FileUpload {
    private static final long BYTES_PER_MB = 1024L * 1024L;

    /**
     * 当前待处理的 multipart 文件。
     */
    final MultipartFile file;

    /**
     * 当前上传使用的配置。
     */
    final FileUploadConfig config;

    /**
     * 创建上传处理器。
     *
     * @param file   待上传文件；在调用 {@link #save()} 或 {@link #check()} 时不得为 {@code null}
     * @param config 上传配置；在调用 {@link #save()} 或 {@link #check()} 时不得为 {@code null}
     */
    public FileUpload(MultipartFile file, FileUploadConfig config) {
        this.file = file;
        this.config = config;
    }

    /**
     * 校验文件并按照配置的存储类型保存。
     *
     * @return 存储实现返回的上传结果，不会返回 {@code null}
     * @throws IllegalArgumentException      文件或配置不合法时抛出
     * @throws IllegalStateException         数据库存储未配置回调或回调返回 {@code null} 时抛出
     * @throws UnsupportedOperationException 选择尚未实现的存储类型时抛出
     * @throws UncheckedIOException          本地目录初始化或写入失败时抛出
     */
    public UploadedResult save() {
        validateConfiguration();
        validateStorage();
        checkFile();

        switch (config.getStorageType()) {
            case LOCAL_DISK:
                return saveToDisk();
            case DATABASE:
                return saveToDatabase();
            case FILE_SERVICE:
            case FILE_SERVICE_API:
                throw new UnsupportedOperationException(
                        "The storage type " + config.getStorageType() + " is not implemented.");
            default:
                throw new UnsupportedOperationException("The storage type is not supported.");
        }
    }

    /**
     * 仅执行配置、大小、扩展名、Content-Type 和魔数校验，不保存文件。
     *
     * @throws IllegalArgumentException 文件或配置不符合规则时抛出
     * @throws UncheckedIOException     读取文件内容进行检测失败时抛出
     */
    public void check() {
        validateConfiguration();
        checkFile();
    }

    private void checkFile() {
        if (file.isEmpty())
            throw new IllegalArgumentException("没有上传任何文件");

        long fileSize = file.getSize();

        if (fileSize < 0)
            throw new IllegalArgumentException("The uploaded file size is invalid.");

        if (fileSize > maxFileSizeInBytes())
            throw new IllegalArgumentException("文件大小超过系统限制！");

        String ext = NamePolicy.getFileExtension(file);

        ExtensionCheck.checkExtName(config, ext);

        if (config.getContentTypePolicy() != ContentTypePolicy.Policy.NO_CHECK)
            new ContentTypePolicy(file, config).check();

        MagicNumber.checkMagicNumber(file, config, ext);
    }

    private void validateConfiguration() {
        if (file == null)
            throw new IllegalArgumentException("The uploaded file is required.");

        if (config == null)
            throw new IllegalArgumentException("The file upload configuration is required.");

        if (config.getStorageType() == null)
            throw new IllegalArgumentException("The storage type is required.");

        if (config.getDetectType() == null)
            throw new IllegalArgumentException("The file detection type is required.");

        if (config.getContentTypePolicy() == null)
            throw new IllegalArgumentException("The content-type policy is required.");

        if (config.getNamePolicy() == null)
            throw new IllegalArgumentException("The file naming policy is required.");

        maxFileSizeInBytes();
    }

    private long maxFileSizeInBytes() {
        long maxFileSize = config.getMaxFileSize();

        if (maxFileSize <= 0)
            throw new IllegalArgumentException("The maximum file size must be greater than zero.");

        try {
            return Math.multiplyExact(maxFileSize, BYTES_PER_MB);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "The maximum file size is too large: " + maxFileSize + " MB.", e);
        }
    }

    private void validateStorage() {
        switch (config.getStorageType()) {
            case LOCAL_DISK:
                return;
            case DATABASE:
                if (saveToDatabase == null)
                    throw new IllegalStateException(
                            "A database storage callback must be configured before saving.");
                return;
            case FILE_SERVICE:
            case FILE_SERVICE_API:
                throw new UnsupportedOperationException(
                        "The storage type " + config.getStorageType() + " is not implemented.");
            default:
                throw new UnsupportedOperationException("The storage type is not supported.");
        }
    }

    private UploadedResult saveToDisk() {
        Path dir = initDir();
        String fileName = new NamePolicy(file, config.getNamePolicy()).getFileName();
        Path dest = dir.resolve(fileName).normalize();

        if (!dest.startsWith(dir))
            throw new IllegalArgumentException("The target file escapes the upload directory.");

        try {
            if (Files.isSymbolicLink(dest))
                throw new IllegalArgumentException("The target file must not be a symbolic link.");

            file.transferTo(dest.toFile());// 保存文件
            log.info("File saved to: {}", dest);
        } catch (IOException e) {
            log.error("Error occurred when saving file to {}", dest, e);
            throw new UncheckedIOException("Error occurred when saving file to " + dest, e);
        }

        String fileUrl = ShowUrlPolicy.concatTwoUrl(config.getUrlPrefix(), fileName);
        // 返回数据
        UploadedResult result = new UploadedResult();
        result.setUrl(fileUrl);
        result.setFileName(fileName);
        result.setOriginalFileName(file.getOriginalFilename());
        result.setFileSize(file.getSize());

        return result;
    }

    private Path initDir() {
        String configuredBaseDir = config.getBaseUploadDir();

        if (ObjectHelper.isEmptyText(configuredBaseDir))
            throw new UnsupportedOperationException("The config of upload dir is not given.");

        Path baseDir = Paths.get(configuredBaseDir).toAbsolutePath().normalize();
        String subDir = config.getUploadDir();
        Path dir = baseDir;

        if (ObjectHelper.hasText(subDir)) {
            Path relativeDir = Paths.get(subDir);

            if (relativeDir.isAbsolute())
                throw new IllegalArgumentException("The upload subdirectory must be relative.");

            dir = baseDir.resolve(relativeDir).normalize();

            if (!dir.startsWith(baseDir))
                throw new IllegalArgumentException("The upload subdirectory escapes the base directory.");
        }

        try {
            Files.createDirectories(baseDir);
            Path realBaseDir = baseDir.toRealPath();

            rejectSymbolicLinks(baseDir, dir);
            Files.createDirectories(dir);

            Path realDir = dir.toRealPath();

            if (!realDir.startsWith(realBaseDir))
                throw new IllegalArgumentException("The upload subdirectory escapes the base directory.");

            PermissionCheck.check(realDir.toString());

            return realDir;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to initialize upload directory: " + dir, e);
        }
    }

    private static void rejectSymbolicLinks(Path baseDir, Path dir) {
        Path current = baseDir;

        for (Path part : baseDir.relativize(dir)) {
            current = current.resolve(part);

            if (Files.isSymbolicLink(current))
                throw new IllegalArgumentException("The upload subdirectory must not contain symbolic links.");
        }
    }

    /**
     * 根据域名、应用上下文路径和上传路径设置文件访问 URL 前缀。
     *
     * @param baseUrl     域名，例如 {@code https://example.com}
     * @param contextPath 应用上下文路径，例如 {@code /app}
     * @param uploadPath  上传资源路径，例如 {@code /uploads}
     */
    public void setUrlPrefix(String baseUrl, String contextPath, String uploadPath) {
        String urlPrefix = UrlHelper.concatUrl(baseUrl, contextPath);
        urlPrefix = UrlHelper.concatUrl(urlPrefix, uploadPath);

        config.setUrlPrefix(urlPrefix);
    }

    /**
     * 数据库存储回调；仅当 {@link com.ajaxjs.fileupload.policy.StorageType#DATABASE} 时使用。
     * 回调必须返回非 {@code null} 的 {@link UploadedResult}。
     */
    BiFunction<MultipartFile, FileUploadConfig, UploadedResult> saveToDatabase;

    private UploadedResult saveToDatabase() {
        UploadedResult result = saveToDatabase.apply(file, config);

        if (result == null)
            throw new IllegalStateException("The database storage callback returned no upload result.");

        return result;
    }
}
