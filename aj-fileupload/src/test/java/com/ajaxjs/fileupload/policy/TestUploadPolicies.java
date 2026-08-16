package com.ajaxjs.fileupload.policy;

import com.ajaxjs.fileupload.DetectType;
import com.ajaxjs.fileupload.FileUploadConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for upload policies, including name extraction, extension validation,
 * detect-type-based extension sets, content type whitelisting, and URL concatenation.
 */
class TestUploadPolicies {

    /**
     * Verifies that {@link NamePolicy#getBaseName} and {@link NamePolicy#getFileExtension}
     * correctly extract the base name and last extension from file names.
     */
    @Test
    void extractsBaseNameAndLastExtension() {
        assertEquals("archive.tar", NamePolicy.getBaseName("archive.tar.gz"));
        assertEquals("gz", NamePolicy.getFileExtension("archive.tar.gz"));
        assertEquals("README", NamePolicy.getBaseName("README"));
    }

    /**
     * Verifies that {@link NamePolicy#getFileExtension} throws
     * {@link IllegalArgumentException} for {@code null} input or file names
     * without an extension.
     */
    @Test
    void extensionRequiresDotAndFilename() {
        assertThrows(IllegalArgumentException.class, () -> NamePolicy.getFileExtension((String) null));
        assertThrows(IllegalArgumentException.class, () -> NamePolicy.getFileExtension("README"));
    }

    /**
     * Verifies that all naming policies (ORIGINAL, ORIGINAL_RANDOM, RANDOM)
     * produce safe file names without path separators.
     */
    @Test
    void namingPoliciesPreserveOnlySafeParts() {
        assertEquals(
                "report.txt",
                new NamePolicy("C:\\fakepath\\report.txt", NamePolicy.Policy.ORIGINAL).getFileName()
        );

        String originalRandom =
                new NamePolicy("../../report.txt", NamePolicy.Policy.ORIGINAL_RANDOM).getFileName();
        assertTrue(originalRandom.startsWith("report_"));
        assertTrue(originalRandom.endsWith(".txt"));
        assertFalse(originalRandom.contains("/"));

        String random = new NamePolicy("report.txt", NamePolicy.Policy.RANDOM).getFileName();
        assertTrue(random.endsWith(".txt"));
        assertFalse(random.contains("report"));
    }

    /**
     * Verifies that custom extension lists are matched case-insensitively.
     */
    @Test
    void customExtensionListIsCaseInsensitive() {
        assertDoesNotThrow(() -> ExtensionCheck.checkExtName(new String[]{"jpg", "PNG"}, "png"));
        assertThrows(
                IllegalArgumentException.class,
                () -> ExtensionCheck.checkExtName(new String[]{"jpg", "png"}, "exe")
        );
    }

    /**
     * Verifies that the {@link DetectType} setting applies its built-in extension
     * set, and that {@code DetectType.NONE} allows any extension.
     */
    @Test
    void detectTypeAppliesItsExtensionSet() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.IMAGE);

        assertDoesNotThrow(() -> ExtensionCheck.checkExtName(config, "JPEG"));
        assertThrows(IllegalArgumentException.class, () -> ExtensionCheck.checkExtName(config, "pdf"));

        config.setDetectType(DetectType.NONE);
        assertDoesNotThrow(() -> ExtensionCheck.checkExtName(config, "unknown"));
    }

    /**
     * Verifies that the content type whitelist policy accepts matching MIME types
     * when the upload is an image.
     */
    @Test
    void contentTypeWhitelistAcceptsMatchingCategory() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.IMAGE);
        config.setContentTypePolicy(ContentTypePolicy.Policy.WHITELIST);
        MockMultipartFile png =
                new MockMultipartFile("file", "image.png", "image/png", new byte[]{1});

        assertDoesNotThrow(() -> new ContentTypePolicy(png, config).check());
    }

    /**
     * Verifies that the content type whitelist policy rejects non-matching
     * MIME types and missing (null) content types.
     */
    @Test
    void contentTypeWhitelistRejectsWrongOrMissingType() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.IMAGE);
        config.setContentTypePolicy(ContentTypePolicy.Policy.WHITELIST);

        MockMultipartFile wrong =
                new MockMultipartFile("file", "image.png", "application/pdf", new byte[]{1});
        MockMultipartFile missing =
                new MockMultipartFile("file", "image.png", null, new byte[]{1});

        assertThrows(IllegalArgumentException.class, () -> new ContentTypePolicy(wrong, config).check());
        assertThrows(IllegalArgumentException.class, () -> new ContentTypePolicy(missing, config).check());
    }

    /**
     * Verifies that {@link ShowUrlPolicy#concatTwoUrl} correctly normalizes
     * exactly one slash at the boundary between two URL parts.
     */
    @Test
    void urlConcatenationNormalizesOneBoundarySlash() {
        assertEquals(
                "https://example.com/files/report.txt",
                ShowUrlPolicy.concatTwoUrl("https://example.com/files", "/report.txt")
        );
        assertEquals(
                "https://example.com/files/report.txt",
                ShowUrlPolicy.concatTwoUrl("https://example.com/files/", "report.txt")
        );
    }
}