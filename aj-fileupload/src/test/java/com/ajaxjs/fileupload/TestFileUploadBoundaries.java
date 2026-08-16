package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for boundary conditions in {@link FileUpload}, including null inputs,
 * empty files, exact and over-limit sizes, negative reported sizes, null policies,
 * and URL prefix building.
 */
class TestFileUploadBoundaries {

    /**
     * Verifies that creating a {@link FileUpload} with a {@code null} file or
     * {@code null} configuration throws {@link IllegalArgumentException}.
     */
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

    /**
     * Verifies that an empty file (zero bytes) is rejected.
     */
    @Test
    void rejectsEmptyFile() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FileUpload(file(new byte[0]), configuration()).check()
        );
    }

    /**
     * Verifies that a file exactly at the maximum size limit is accepted,
     * while a file one byte over is rejected.
     */
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

    /**
     * Verifies that a file reporting a negative size via {@code getSize()}
     * is rejected.
     */
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

    /**
     * Verifies that setting a {@code null} detect type, content type policy,
     * or name policy on the configuration is rejected.
     */
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

    /**
     * Verifies that the URL prefix is correctly built from three parts,
     * producing a single concatenated string.
     */
    @Test
    void buildsUrlPrefixFromThreeParts() {
        FileUploadConfig config = configuration();
        FileUpload upload = new FileUpload(file(new byte[]{1}), config);

        upload.setUrlPrefix("https://example.com", "/app", "/files");

        assertEquals("https://example.com/app/files", config.getUrlPrefix());
    }

    /**
     * Creates a default {@link FileUploadConfig} with a no-check content type
     * policy and magic number check disabled.
     *
     * @return a configured {@link FileUploadConfig} instance
     */
    private static FileUploadConfig configuration() {
        FileUploadConfig config = new FileUploadConfig();
        config.setContentTypePolicy(ContentTypePolicy.Policy.NO_CHECK);
        config.setCheckMagicNumber(false);
        return config;
    }

    /**
     * Creates a {@link MockMultipartFile} with the given byte content.
     *
     * @param content the file content bytes
     * @return a configured {@link MockMultipartFile} instance
     */
    private static MockMultipartFile file(byte[] content) {
        return new MockMultipartFile(
                "file",
                "data.bin",
                "application/octet-stream",
                content
        );
    }
}