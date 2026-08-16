package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.fileupload.policy.NamePolicy;
import com.ajaxjs.fileupload.policy.StorageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for upload path security, ensuring that file names and upload directories
 * are sanitized and cannot escape the designated base directory.
 */
class TestUploadPathSecurity {

    /**
     * Temporary directory used as the base upload directory for tests.
     */
    @TempDir
    Path tempDir;

    /**
     * Verifies that Unix ({@code ../}) and Windows ({@code C:\fakepath\})
     * path components are stripped from the original file name.
     */
    @Test
    void removesUnixAndWindowsPathComponentsFromOriginalName() {
        assertEquals("avatar.png", NamePolicy.sanitizeFileName("../../avatar.png"));
        assertEquals("avatar.png", NamePolicy.sanitizeFileName("C:\\fakepath\\avatar.png"));
    }

    /**
     * Verifies that file names consisting only of traversal sequences
     * or containing control characters are rejected.
     */
    @Test
    void rejectsInvalidOriginalNames() {
        assertThrows(IllegalArgumentException.class, () -> NamePolicy.sanitizeFileName("../"));
        assertThrows(IllegalArgumentException.class, () -> NamePolicy.sanitizeFileName("bad\nname.png"));
    }

    /**
     * Verifies that the ORIGINAL naming policy cannot write files outside the
     * upload directory, even when the original file name contains path traversal.
     *
     * @throws Exception if an I/O error occurs
     */
    @Test
    void originalPolicyCannotWriteOutsideUploadDirectory() throws Exception {
        FileUpload upload = upload("../../outside.txt", null);
        UploadedResult result = upload.save();

        assertEquals("outside.txt", result.getFileName());
        assertTrue(Files.exists(tempDir.resolve("outside.txt")));
        assertFalse(Files.exists(tempDir.getParent().resolve("outside.txt")));
    }

    /**
     * Verifies that the ORIGINAL_RANDOM naming policy does not preserve
     * client-supplied path separators in the generated file name.
     */
    @Test
    void originalRandomPolicyCannotPreserveClientPath() {
        String generated = new NamePolicy("../../outside.txt", NamePolicy.Policy.ORIGINAL_RANDOM).getFileName();

        assertTrue(generated.startsWith("outside_"));
        assertFalse(generated.contains("/"));
        assertFalse(generated.contains("\\"));
    }

    /**
     * Verifies that an upload directory specified as a relative path that
     * escapes the base directory is rejected.
     */
    @Test
    void rejectsUploadDirectoryOutsideBaseDirectory() {
        FileUpload upload = upload("safe.txt", "../outside");

        assertThrows(IllegalArgumentException.class, upload::save);
    }

    /**
     * Verifies that an absolute upload directory path is rejected.
     */
    @Test
    void rejectsAbsoluteUploadDirectory() {
        FileUpload upload = upload("safe.txt", tempDir.getParent().toString());

        assertThrows(IllegalArgumentException.class, upload::save);
    }

    /**
     * Creates a {@link FileUpload} instance configured with the given original
     * file name and upload directory for path security testing.
     *
     * @param originalFilename the original file name (may contain path traversal)
     * @param uploadDir        the upload directory relative to the temp dir, or {@code null}
     * @return a configured {@link FileUpload} instance
     */
    private FileUpload upload(String originalFilename, String uploadDir) {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                originalFilename,
                "text/plain",
                "content".getBytes(StandardCharsets.UTF_8)
        );
        FileUploadConfig config = new FileUploadConfig();
        config.setStorageType(StorageType.LOCAL_DISK);
        config.setMaxFileSize(1);
        config.setBaseUploadDir(tempDir.toString());
        config.setUploadDir(uploadDir);
        config.setDetectType(DetectType.NONE);
        config.setCheckMagicNumber(false);
        config.setContentTypePolicy(ContentTypePolicy.Policy.NO_CHECK);
        config.setNamePolicy(NamePolicy.Policy.ORIGINAL);
        config.setUrlPrefix("/files");

        return new FileUpload(file, config);
    }
}