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
 * Validation and save handler for a single {@link MultipartFile}.
 * <p>An instance binds one upload file and one configuration and should not be reused across multiple upload requests.</p>
 */
@Slf4j
@Data
public class FileUpload {
    /**
     * Number of bytes in one megabyte.
     */
    private static final long BYTES_PER_MB = 1024L * 1024L;

    /**
     * The multipart file currently being processed.
     */
    final MultipartFile file;

    /**
     * The configuration used for the current upload.
     */
    final FileUploadConfig config;

    /**
     * Creates an upload handler.
     *
     * @param file   File to upload; must not be {@code null} when calling {@link #save()} or {@link #check()}
     * @param config Upload configuration; must not be {@code null} when calling {@link #save()} or {@link #check()}
     */
    public FileUpload(MultipartFile file, FileUploadConfig config) {
        this.file = file;
        this.config = config;
    }

    /**
     * Validates the file and saves it according to the configured storage type.
     *
     * @return Upload result from the storage implementation; never returns {@code null}
     * @throws IllegalArgumentException      If the file or configuration is invalid
     * @throws IllegalStateException         If database storage is selected but no callback is configured or the callback returns {@code null}
     * @throws UnsupportedOperationException If a storage type that is not yet implemented is selected
     * @throws UncheckedIOException          If local directory initialization or file writing fails
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
     * Only performs configuration, size, extension, Content-Type, and magic number validation without saving the file.
     *
     * @throws IllegalArgumentException If the file or configuration does not conform to rules
     * @throws UncheckedIOException      If reading file content for detection fails
     */
    public void check() {
        validateConfiguration();
        checkFile();
    }

    /**
     * Validates the uploaded file: checks emptiness, size, extension, Content-Type, and magic number.
     *
     * @throws IllegalArgumentException If the file is empty, the size is invalid, or exceeds the limit
     * @throws IllegalArgumentException If the file extension is not allowed
     * @throws UncheckedIOException      If reading file content for detection fails
     */
    void checkFile() {
        if (file.isEmpty())
            throw new IllegalArgumentException("No file was uploaded");

        long fileSize = file.getSize();

        if (fileSize < 0)
            throw new IllegalArgumentException("The uploaded file size is invalid.");

        if (fileSize > maxFileSizeInBytes())
            throw new IllegalArgumentException("File size exceeds the system limit!");

        String ext = NamePolicy.getFileExtension(file);

        ExtensionCheck.checkExtName(config, ext);

        if (config.getContentTypePolicy() != ContentTypePolicy.Policy.NO_CHECK)
            new ContentTypePolicy(file, config).check();

        MagicNumber.checkMagicNumber(file, config, ext);
    }

    /**
     * Validates that the file, configuration, and all required configuration properties are present and valid.
     *
     * @throws IllegalArgumentException If the file, configuration, or any required property is null or invalid
     */
    void validateConfiguration() {
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

    /**
     * Converts the configured maximum file size from megabytes to bytes and validates the value.
     *
     * @return Maximum file size in bytes
     * @throws IllegalArgumentException If the configured max file size is zero or negative, or causes overflow
     */
    long maxFileSizeInBytes() {
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

    /**
     * Validates that the selected storage type is supported and, if required, that the necessary callback is configured.
     *
     * @throws IllegalStateException         If database storage is selected but no callback is configured
     * @throws UnsupportedOperationException If the storage type is not implemented or not supported
     */
    void validateStorage() {
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

    /**
     * Saves the uploaded file to local disk and returns the upload result.
     *
     * @return Upload result containing the file URL, saved file name, original file name, and file size
     * @throws IllegalArgumentException If the target file escapes the upload directory or is a symbolic link
     * @throws UncheckedIOException      If file writing fails
     */
    UploadedResult saveToDisk() {
        Path dir = initDir();
        String fileName = new NamePolicy(file, config.getNamePolicy()).getFileName();
        Path destination = dir.resolve(fileName).normalize();

        if (!destination.startsWith(dir))
            throw new IllegalArgumentException("The target file escapes the upload directory.");

        try {
            if (Files.isSymbolicLink(destination))
                throw new IllegalArgumentException("The target file must not be a symbolic link.");

            file.transferTo(destination.toFile());// Save file
            log.info("File saved to: {}", destination);
        } catch (IOException e) {
            log.error("Error occurred when saving file to {}", destination, e);
            throw new UncheckedIOException("Error occurred when saving file to " + destination, e);
        }

        String fileUrl = ShowUrlPolicy.concatTwoUrl(config.getUrlPrefix(), fileName);
        // Return data
        UploadedResult result = new UploadedResult();
        result.setUrl(fileUrl);
        result.setFileName(fileName);
        result.setOriginalFileName(file.getOriginalFilename());
        result.setFileSize(file.getSize());

        return result;
    }

    /**
     * Initializes and validates the upload directory, creating it if necessary.
     *
     * @return The resolved real path of the upload directory
     * @throws IllegalArgumentException If the subdirectory is absolute, escapes the base directory, or contains symbolic links
     * @throws UnsupportedOperationException If the base upload directory is not configured
     * @throws UncheckedIOException          If directory creation or path resolution fails
     */
    Path initDir() {
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

    /**
     * Checks that no component of the relative path between {@code baseDir} and {@code dir} is a symbolic link.
     *
     * @param baseDir The base directory
     * @param dir     The target directory to check
     * @throws IllegalArgumentException If any path component is a symbolic link
     */
    static void rejectSymbolicLinks(Path baseDir, Path dir) {
        Path current = baseDir;

        for (Path part : baseDir.relativize(dir)) {
            current = current.resolve(part);

            if (Files.isSymbolicLink(current))
                throw new IllegalArgumentException("The upload subdirectory must not contain symbolic links.");
        }
    }

    /**
     * Sets the file access URL prefix based on domain, application context path, and upload a path.
     *
     * @param baseUrl     Domain, e.g. {@code https://example.com}
     * @param contextPath Application context path, e.g. {@code /app}
     * @param uploadPath  Upload resource path, e.g. {@code /uploads}
     */
    public void setUrlPrefix(String baseUrl, String contextPath, String uploadPath) {
        String urlPrefix = UrlHelper.concatUrl(baseUrl, contextPath);
        urlPrefix = UrlHelper.concatUrl(urlPrefix, uploadPath);

        config.setUrlPrefix(urlPrefix);
    }

    /**
     * Database storage callback; used only when {@link com.ajaxjs.fileupload.policy.StorageType#DATABASE}.
     * The callback must return a non-{@code null} {@link UploadedResult}.
     */
    BiFunction<MultipartFile, FileUploadConfig, UploadedResult> saveToDatabase;

    /**
     * Delegates saving to the configured database callback and validating the result.
     *
     * @return Upload result from the database callback
     * @throws IllegalStateException If the callback returns {@code null}
     */
    UploadedResult saveToDatabase() {
        UploadedResult result = saveToDatabase.apply(file, config);

        if (result == null)
            throw new IllegalStateException("The database storage callback returned no upload result.");

        return result;
    }
}
