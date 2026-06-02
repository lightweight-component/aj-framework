package com.ajaxjs.fileupload;

import com.ajaxjs.util.reflect.Methods;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
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
        return doUpload(controllerClz, methodName, file, customConfig, null);
    }

    public static UploadedResult doUpload(Class<?> controllerClz, String methodName, MultipartFile file,
                                          Consumer<FileUploadConfig> customConfig,
                                          BiFunction<MultipartFile, FileUploadConfig, UploadedResult> saveToDatabase) {
        Method uploadMethod = Methods.getMethod(controllerClz, methodName, MultipartFile.class);

        if (uploadMethod == null) {
            // try again
            uploadMethod = Methods.getMethod(controllerClz, methodName, MultipartFile.class, HttpServletResponse.class);

            if (uploadMethod == null)
                throw new UnsupportedOperationException("Failed to get controller method.");
        }

        if (!uploadMethod.isAnnotationPresent(FileUploadAction.class))
            throw new UnsupportedOperationException("It's not a file upload controller.");

        FileUploadAction annotation = uploadMethod.getAnnotation(FileUploadAction.class);
        FileUploadConfig fileUploadConfig = fromAnnotation(annotation);

        if (customConfig != null)
            customConfig.accept(fileUploadConfig);

        FileUpload fileUpload = new FileUpload(file, fileUploadConfig);
        fileUpload.setSaveToDatabase(saveToDatabase);

        return fileUpload.save();
    }
}
