package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.policy.StorageType;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FileUpload} configuration validation, including default values,
 * storage-type-specific behaviour, and invalid maximum size rejection.
 */
class TestFileUploadConfiguration {

    /**
     * Verifies that a newly created {@link FileUploadConfig} has sensible defaults
     * for storage type, detect type, max file size, URL prefix, and policies.
     */
    @Test
    void directConfigurationHasUsableDefaults() {
        FileUploadConfig config = new FileUploadConfig();

        assertEquals(StorageType.LOCAL_DISK, config.getStorageType());
        assertEquals(DetectType.NONE, config.getDetectType());
        assertEquals(10, config.getMaxFileSize());
        assertEquals("", config.getUrlPrefix());
        assertNotNull(config.getContentTypePolicy());
        assertNotNull(config.getNamePolicy());
    }

    /**
     * Verifies that database storage requires a database callback and throws
     * {@link IllegalStateException} when none is set.
     */
    @Test
    void databaseStorageRequiresCallback() {
        FileUploadConfig config = configuration(StorageType.DATABASE);

        assertThrows(IllegalStateException.class, () -> upload(config).save());
    }

    /**
     * Verifies that database storage with a callback that returns {@code null}
     * throws {@link IllegalStateException}.
     */
    @Test
    void databaseStorageRejectsNullResult() {
        FileUpload upload = upload(configuration(StorageType.DATABASE));
        upload.setSaveToDatabase((file, config) -> null);

        assertThrows(IllegalStateException.class, upload::save);
    }

    /**
     * Verifies that database storage returns the result from the callback
     * when the callback provides a valid {@link UploadedResult}.
     */
    @Test
    void databaseStorageReturnsCallbackResult() {
        FileUpload upload = upload(configuration(StorageType.DATABASE));
        UploadedResult expected = new UploadedResult();
        upload.setSaveToDatabase((file, config) -> expected);

        assertEquals(expected, upload.save());
    }

    /**
     * Verifies that unsupported storage types ({@code FILE_SERVICE} and
     * {@code FILE_SERVICE_API}) throw {@link UnsupportedOperationException}.
     */
    @Test
    void unimplementedFileServicesFailExplicitly() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> upload(configuration(StorageType.FILE_SERVICE)).save()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> upload(configuration(StorageType.FILE_SERVICE_API)).save()
        );
    }

    /**
     * Verifies that invalid maximum file size values (zero, negative, or
     * {@link Long#MAX_VALUE}) are rejected.
     */
    @Test
    void rejectsInvalidAndOverflowingMaximumSize() {
        FileUploadConfig config = configuration(StorageType.DATABASE);
        config.setMaxFileSize(0);
        assertThrows(IllegalArgumentException.class, () -> upload(config).check());

        config.setMaxFileSize(-1);
        assertThrows(IllegalArgumentException.class, () -> upload(config).check());

        config.setMaxFileSize(Long.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> upload(config).check());
    }

    /**
     * Creates a {@link FileUploadConfig} with the given storage type.
     *
     * @param storageType the storage type to set
     * @return a configured {@link FileUploadConfig} instance
     */
    private static FileUploadConfig configuration(StorageType storageType) {
        FileUploadConfig config = new FileUploadConfig();
        config.setStorageType(storageType);
        return config;
    }

    /**
     * Creates a {@link FileUpload} instance with a minimal multipart file and
     * the given configuration.
     *
     * @param config the upload configuration
     * @return a configured {@link FileUpload} instance
     */
    private static FileUpload upload(FileUploadConfig config) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                new byte[]{1}
        );

        return new FileUpload(file, config);
    }
}