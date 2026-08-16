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

class TestUploadPathSecurity {
    @TempDir
    Path tempDir;

    @Test
    void removesUnixAndWindowsPathComponentsFromOriginalName() {
        assertEquals("avatar.png", NamePolicy.sanitizeFileName("../../avatar.png"));
        assertEquals("avatar.png", NamePolicy.sanitizeFileName("C:\\fakepath\\avatar.png"));
    }

    @Test
    void rejectsInvalidOriginalNames() {
        assertThrows(IllegalArgumentException.class, () -> NamePolicy.sanitizeFileName("../"));
        assertThrows(IllegalArgumentException.class, () -> NamePolicy.sanitizeFileName("bad\nname.png"));
    }

    @Test
    void originalPolicyCannotWriteOutsideUploadDirectory() throws Exception {
        FileUpload upload = upload("../../outside.txt", null);
        UploadedResult result = upload.save();

        assertEquals("outside.txt", result.getFileName());
        assertTrue(Files.exists(tempDir.resolve("outside.txt")));
        assertFalse(Files.exists(tempDir.getParent().resolve("outside.txt")));
    }

    @Test
    void originalRandomPolicyCannotPreserveClientPath() {
        String generated = new NamePolicy("../../outside.txt", NamePolicy.Policy.ORIGINAL_RANDOM).getFileName();

        assertTrue(generated.startsWith("outside_"));
        assertFalse(generated.contains("/"));
        assertFalse(generated.contains("\\"));
    }

    @Test
    void rejectsUploadDirectoryOutsideBaseDirectory() {
        FileUpload upload = upload("safe.txt", "../outside");

        assertThrows(IllegalArgumentException.class, upload::save);
    }

    @Test
    void rejectsAbsoluteUploadDirectory() {
        FileUpload upload = upload("safe.txt", tempDir.getParent().toString());

        assertThrows(IllegalArgumentException.class, upload::save);
    }

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
