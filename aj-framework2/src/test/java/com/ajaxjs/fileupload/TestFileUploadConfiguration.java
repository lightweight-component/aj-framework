package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.policy.StorageType;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestFileUploadConfiguration {
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

    @Test
    void databaseStorageRequiresCallback() {
        FileUploadConfig config = configuration(StorageType.DATABASE);

        assertThrows(IllegalStateException.class, () -> upload(config).save());
    }

    @Test
    void databaseStorageRejectsNullResult() {
        FileUpload upload = upload(configuration(StorageType.DATABASE));
        upload.setSaveToDatabase((file, config) -> null);

        assertThrows(IllegalStateException.class, upload::save);
    }

    @Test
    void databaseStorageReturnsCallbackResult() {
        FileUpload upload = upload(configuration(StorageType.DATABASE));
        UploadedResult expected = new UploadedResult();
        upload.setSaveToDatabase((file, config) -> expected);

        assertEquals(expected, upload.save());
    }

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

    private static FileUploadConfig configuration(StorageType storageType) {
        FileUploadConfig config = new FileUploadConfig();
        config.setStorageType(storageType);
        return config;
    }

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
