package com.ajaxjs.fileupload;

import com.ajaxjs.fileupload.policy.ContentTypePolicy;
import com.ajaxjs.fileupload.policy.NamePolicy;
import com.ajaxjs.fileupload.policy.StorageType;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class TestUploadUtils {
    public static class Controller {
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

        public void noAnnotation(MultipartFile file) {
        }
    }

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
