package com.ajaxjs.fileupload.policy;

import com.ajaxjs.fileupload.DetectType;
import com.ajaxjs.fileupload.FileUploadConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class TestContentTypePolicy {

    // ── simpleCheck() normal cases ──

    @Test
    void simpleCheckNoneAllowsAnyContentType() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.NONE);
        MockMultipartFile file = new MockMultipartFile("file", "test.xyz", "application/octet-stream", new byte[]{1});
        assertDoesNotThrow(() -> new ContentTypePolicy(file, config).simpleCheck());
    }

    @Test
    void simpleCheckImageAcceptsValidImageType() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.IMAGE);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[]{1});
        assertDoesNotThrow(() -> new ContentTypePolicy(file, config).simpleCheck());
    }

    @Test
    void simpleCheckOfficeFileAcceptsValidOfficeType() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.OFFICE_FILE);
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[]{1});
        assertDoesNotThrow(() -> new ContentTypePolicy(file, config).simpleCheck());
    }

    @Test
    void simpleCheckAudioAcceptsValidAudioType() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.AUDIO);
        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", new byte[]{1});
        assertDoesNotThrow(() -> new ContentTypePolicy(file, config).simpleCheck());
    }

    @Test
    void simpleCheckVideoAcceptsValidVideoType() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.VIDEO);
        MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", new byte[]{1});
        assertDoesNotThrow(() -> new ContentTypePolicy(file, config).simpleCheck());
    }

    // ── simpleCheck() exception cases ──

    @Test
    void simpleCheckImageRejectsNonImageType() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.IMAGE);
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[]{1});
        assertThrows(IllegalArgumentException.class, () -> new ContentTypePolicy(file, config).simpleCheck());
    }

    @Test
    void simpleCheckOfficeFileRejectsNonOfficeType() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.OFFICE_FILE);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[]{1});
        assertThrows(IllegalArgumentException.class, () -> new ContentTypePolicy(file, config).simpleCheck());
    }

    @Test
    void simpleCheckAudioRejectsNonAudioType() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.AUDIO);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[]{1});
        assertThrows(IllegalArgumentException.class, () -> new ContentTypePolicy(file, config).simpleCheck());
    }

    @Test
    void simpleCheckVideoRejectsNonVideoType() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.VIDEO);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[]{1});
        assertThrows(IllegalArgumentException.class, () -> new ContentTypePolicy(file, config).simpleCheck());
    }

    // ── checkMapping() tests ──

    @Test
    void checkMappingDoesNotThrowForValidExtension() {
        FileUploadConfig config = new FileUploadConfig();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", new byte[]{1});
        assertDoesNotThrow(() -> new ContentTypePolicy(file, config).checkMapping());
    }

    @Test
    void checkMappingDoesNotThrowForMismatchedExtension() {
        // The comparison logic in checkMapping() is currently commented out,
        // so mismatched extensions do not throw an exception.
        FileUploadConfig config = new FileUploadConfig();
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "image/png", new byte[]{1});
        assertDoesNotThrow(() -> new ContentTypePolicy(file, config).checkMapping());
    }
}