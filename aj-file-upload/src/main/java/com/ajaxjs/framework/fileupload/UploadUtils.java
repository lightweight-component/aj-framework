package com.ajaxjs.framework.fileupload;

import com.ajaxjs.util.reflect.Methods;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * File upload utilities.
 */
public class UploadUtils {
    /**
     * Create a configuration from an annotation.
     *
     * @param annotation Annotation
     * @return Configuration
     */
    public static FileUploadConfig fromAnnotation(FileUploadAction annotation) {
        FileUploadConfig config = new FileUploadConfig();
        config.setStorageType(annotation.storageType());
        config.setMaxFileSize(annotation.maxFileSize());
        config.setBaseUploadDir(annotation.baseUploadDir());
        config.setUploadDir(annotation.uploadDir());
        config.setAllowExtFilenames(annotation.allowExtFilenames());
        config.setDetectType(annotation.detectType());
        config.setCheckMagicNumber(annotation.checkMagicNumber());
        config.setContentTypePolicy(annotation.contentTypePolicy());
        config.setNamePolicy(annotation.namePolicy());
        config.setUrlPrefix(annotation.urlPrefix());

        return config;
    }

    /**
     * Upload a file.
     *
     * @param controllerClz Controller class
     * @param methodName    Method name
     * @param file          File to upload
     * @return Uploaded result
     */
    public static UploadedResult doUpload(Class<?> controllerClz, String methodName, MultipartFile file) {
        return doUpload(controllerClz, methodName, file, null);
    }

    /**
     * Upload a file.
     *
     * @param controllerClz Controller class
     * @param methodName    Method name
     * @param file          File to upload
     * @param customConfig  Custom configuration
     * @return Uploaded result
     */
    public static UploadedResult doUpload(Class<?> controllerClz, String methodName, MultipartFile file, Consumer<FileUploadConfig> customConfig) {
        Method uploadAudio = Methods.getMethod(controllerClz, methodName, MultipartFile.class);

        if (uploadAudio == null)
            throw new UnsupportedOperationException("Failed to get controller method.");

        if (!uploadAudio.isAnnotationPresent(FileUploadAction.class))
            throw new UnsupportedOperationException("It's not a file upload controller.");

        FileUploadAction annotation = uploadAudio.getAnnotation(FileUploadAction.class);
        FileUploadConfig fileUploadConfig = fromAnnotation(annotation);

        if (customConfig != null)
            customConfig.accept(fileUploadConfig);

        return new FileUpload(file, fileUploadConfig).save();
    }
}
