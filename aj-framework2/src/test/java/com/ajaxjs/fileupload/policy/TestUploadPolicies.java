package com.ajaxjs.fileupload.policy;

import com.ajaxjs.fileupload.DetectType;
import com.ajaxjs.fileupload.FileUploadConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestUploadPolicies {
    @Test
    void extractsBaseNameAndLastExtension() {
        assertEquals("archive.tar", NamePolicy.getBaseName("archive.tar.gz"));
        assertEquals("gz", NamePolicy.getFileExtension("archive.tar.gz"));
        assertEquals("README", NamePolicy.getBaseName("README"));
    }

    @Test
    void extensionRequiresDotAndFilename() {
        assertThrows(IllegalArgumentException.class, () -> NamePolicy.getFileExtension((String) null));
        assertThrows(IllegalArgumentException.class, () -> NamePolicy.getFileExtension("README"));
    }

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

    @Test
    void customExtensionListIsCaseInsensitive() {
        assertDoesNotThrow(() -> ExtensionCheck.checkExtName(new String[]{"jpg", "PNG"}, "png"));
        assertThrows(
                IllegalArgumentException.class,
                () -> ExtensionCheck.checkExtName(new String[]{"jpg", "png"}, "exe")
        );
    }

    @Test
    void detectTypeAppliesItsExtensionSet() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.IMAGE);

        assertDoesNotThrow(() -> ExtensionCheck.checkExtName(config, "JPEG"));
        assertThrows(IllegalArgumentException.class, () -> ExtensionCheck.checkExtName(config, "pdf"));

        config.setDetectType(DetectType.NONE);
        assertDoesNotThrow(() -> ExtensionCheck.checkExtName(config, "unknown"));
    }

    @Test
    void contentTypeWhitelistAcceptsMatchingCategory() {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(DetectType.IMAGE);
        config.setContentTypePolicy(ContentTypePolicy.Policy.WHITELIST);
        MockMultipartFile png =
                new MockMultipartFile("file", "image.png", "image/png", new byte[]{1});

        assertDoesNotThrow(() -> new ContentTypePolicy(png, config).check());
    }

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
