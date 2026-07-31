package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestFileUploadBoundaries {
    @Test
    void rejectsNullFileAndConfiguration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FileUpload(null, new FileUploadConfig()).check()
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new FileUpload(file(new byte[]{1}), null).check()
        );
    }

    @Test
    void rejectsEmptyFile() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FileUpload(file(new byte[0]), configuration()).check()
        );
    }

    @Test
    void acceptsExactMaximumAndRejectsOneByteOver() {
        FileUploadConfig config = configuration();
        config.setMaxFileSize(1);
        byte[] exact = new byte[1024 * 1024];
        byte[] over = new byte[exact.length + 1];

        assertDoesNotThrow(() -> new FileUpload(file(exact), config).check());
        assertThrows(
                IllegalArgumentException.class,
                () -> new FileUpload(file(over), config).check()
        );
    }

    @Test
    void rejectsNegativeReportedFileSize() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "data.bin", "application/octet-stream", new byte[]{1}) {
            @Override
            public long getSize() {
                return -1;
            }
        };

        assertThrows(
                IllegalArgumentException.class,
                () -> new FileUpload(file, configuration()).check()
        );
    }

    @Test
    void rejectsNullPoliciesSetByCaller() {
        FileUploadConfig nullDetectType = configuration();
        nullDetectType.setDetectType(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> new FileUpload(file(new byte[]{1}), nullDetectType).check()
        );

        FileUploadConfig nullContentPolicy = configuration();
        nullContentPolicy.setContentTypePolicy(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> new FileUpload(file(new byte[]{1}), nullContentPolicy).check()
        );

        FileUploadConfig nullNamePolicy = configuration();
        nullNamePolicy.setNamePolicy(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> new FileUpload(file(new byte[]{1}), nullNamePolicy).check()
        );
    }

    @Test
    void buildsUrlPrefixFromThreeParts() {
        FileUploadConfig config = configuration();
        FileUpload upload = new FileUpload(file(new byte[]{1}), config);

        upload.setUrlPrefix("https://example.com", "/app", "/files");

        assertEquals("https://example.com/app/files", config.getUrlPrefix());
    }

    private static FileUploadConfig configuration() {
        FileUploadConfig config = new FileUploadConfig();
        config.setContentTypePolicy(ContentTypePolicy.Policy.NO_CHECK);
        config.setCheckMagicNumber(false);
        return config;
    }

    private static MockMultipartFile file(byte[] content) {
        return new MockMultipartFile(
                "file",
                "data.bin",
                "application/octet-stream",
                content
        );
    }
}
