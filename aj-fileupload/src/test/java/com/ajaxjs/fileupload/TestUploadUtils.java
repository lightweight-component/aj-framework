package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.fileupload.policy.NamePolicy;
import com.ajaxjs.fileupload.policy.StorageType;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link UploadUtils}, covering annotation-to-configuration conversion,
 * {@code doUpload} with database callback, and error handling for missing or
 * unannotated methods.
 */
class TestUploadUtils {

    /**
     * A controller class with an annotated and an unannotated upload method,
     * used to verify annotation-based configuration extraction.
     */
    public static class Controller {

        /**
         * Annotated upload method with all configuration properties set.
         *
         * @param file the multipart file to upload
         */
        @FileUploadAction(
                storageType = StorageType.DATABASE,
                maxFileSize = 7,
                baseUploadDir = "/tmp/upload",
                uploadDir = "documents",
                allowExtFilenames = {"txt", "md"},
                detectType = DetectType.OFFICE_FILE,
                checkMagicNumber = false,
                contentTypePolicy = ContentTypePolicy.Policy.NO_CHECK,
                namePolicy = NamePolicy.Policy.RANDOM,
                urlPrefix = "/files"
        )
        public void upload(MultipartFile file) {
        }

        /**
         * Unannotated upload method, used to verify that unannotated methods are rejected.
         *
         * @param file the multipart file to upload
         */
        public void noAnnotation(MultipartFile file) {
        }
    }

    /**
     * Verifies that every property in the {@link FileUploadAction} annotation
     * is correctly converted to a {@link FileUploadConfig} object.
     *
     * @throws Exception if reflection fails
     */
    @Test
    void convertsEveryAnnotationPropertyToConfiguration() throws Exception {
        Method method = Controller.class.getDeclaredMethod("upload", MultipartFile.class);
        FileUploadConfig config =
                UploadUtils.fromAnnotation(method.getAnnotation(FileUploadAction.class));

        assertEquals(StorageType.DATABASE, config.getStorageType());
        assertEquals(7, config.getMaxFileSize());
        assertEquals("/tmp/upload", config.getBaseUploadDir());
        assertEquals("documents", config.getUploadDir());
        assertEquals(2, config.getAllowExtFilenames().length);
        assertEquals(DetectType.OFFICE_FILE, config.getDetectType());
        assertFalse(config.isCheckMagicNumber());
        assertEquals(ContentTypePolicy.Policy.NO_CHECK, config.getContentTypePolicy());
        assertEquals(NamePolicy.Policy.RANDOM, config.getNamePolicy());
        assertEquals("/files", config.getUrlPrefix());
    }

    /**
     * Verifies that {@link UploadUtils#doUpload} passes the correct file and
     * configuration to the database callback and returns the callback's result.
     */
    @Test
    void annotationUploadUsesDatabaseCallbackAndCustomConfiguration() {
        MockMultipartFile file =
                new MockMultipartFile("file", "notes.txt", "text/plain", new byte[]{1});
        UploadedResult expected = new UploadedResult();
        expected.setFileName("stored.txt");

        UploadedResult actual = UploadUtils.doUpload(
                Controller.class,
                "upload",
                file,
                config -> config.setMaxFileSize(1),
                (uploadedFile, config) -> {
                    assertEquals(file, uploadedFile);
                    assertEquals(1, config.getMaxFileSize());
                    return expected;
                }
        );

        assertEquals(expected, actual);
    }

    /**
     * Verifies that {@link UploadUtils#doUpload} throws
     * {@link UnsupportedOperationException} when the target method is missing
     * or does not have the {@link FileUploadAction} annotation.
     */
    @Test
    void rejectsMissingMethodAndMethodWithoutAnnotation() {
        MockMultipartFile file =
                new MockMultipartFile("file", "notes.txt", "text/plain", new byte[]{1});

        assertThrows(
                UnsupportedOperationException.class,
                () -> UploadUtils.doUpload(Controller.class, "missing", file)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> UploadUtils.doUpload(Controller.class, "noAnnotation", file)
        );
    }
}