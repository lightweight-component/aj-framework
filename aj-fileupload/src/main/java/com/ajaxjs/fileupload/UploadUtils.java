package com.ajaxjs.fileupload;

import com.ajaxjs.util.reflect.Methods;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Utility class for executing uploads based on {@link FileUploadAction}.
 */
public class UploadUtils {
    /**
     * Converts the upload annotation to a standalone configuration object.
     *
     * @param annotation Upload annotation
     * @return Configuration containing all annotation properties
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
     * Uploads a file based on the {@link FileUploadAction} annotation on the controller method.
     *
     * @param controllerClz Controller type
     * @param methodName    Controller method name; the method signature must be a supported upload signature
     * @param file          File to upload
     * @return Upload result
     */
    public static UploadedResult doUpload(Class<?> controllerClz, String methodName, MultipartFile file) {
        return doUpload(controllerClz, methodName, file, null);
    }

    /**
     * Uploads a file based on the annotation on the controller method, and allows the caller to modify the generated configuration.
     *
     * @param controllerClz Controller type
     * @param methodName    Controller method name
     * @param file          File to upload
     * @param customConfig  Configuration modification callback; may be {@code null}
     * @return Upload result
     */
    public static UploadedResult doUpload(Class<?> controllerClz, String methodName, MultipartFile file, Consumer<FileUploadConfig> customConfig) {
        return doUpload(controllerClz, methodName, file, customConfig, null);
    }

    /**
     * Uploads a file based on the annotation on the controller method, with optional configuration modification callback and database save callback.
     * <p>Supported method signatures are {@code (MultipartFile)} or
     * {@code (MultipartFile, HttpServletResponse)}. The target method must be directly annotated with
     * {@link FileUploadAction}.</p>
     *
     * @param controllerClz  Controller type
     * @param methodName     Controller method name
     * @param file           File to upload
     * @param customConfig   Configuration modification callback; may be {@code null}
     * @param saveToDatabase Database storage callback; must be provided and return a non-{@code null} result when using
     *                       {@link com.ajaxjs.fileupload.policy.StorageType#DATABASE}
     * @return Upload result
     * @throws UnsupportedOperationException If no method with a supported signature is found or the method does not have the upload annotation
     */
    public static UploadedResult doUpload(Class<?> controllerClz, String methodName, MultipartFile file,
                                          Consumer<FileUploadConfig> customConfig,
        BiFunction<MultipartFile, FileUploadConfig, UploadedResult> saveToDatabase) {
        Methods methods = new Methods(controllerClz);
        Method uploadMethod = methods.findDeclaredMethodByTypes(methodName, MultipartFile.class);

        if (uploadMethod == null) {
            // try again
            uploadMethod = methods.findDeclaredMethodByTypes(
                    methodName, MultipartFile.class, HttpServletResponse.class);

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
