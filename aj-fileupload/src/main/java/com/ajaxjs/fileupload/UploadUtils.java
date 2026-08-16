package com.ajaxjs.fileupload;

import com.ajaxjs.util.reflect.Methods;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 基于 {@link FileUploadAction} 执行上传的工具类。
 */
public class UploadUtils {
    /**
     * 将上传注解转换为独立的配置对象。
     *
     * @param annotation 上传注解
     * @return 包含注解全部属性的配置
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
     * 根据控制器方法上的 {@link FileUploadAction} 上传文件。
     *
     * @param controllerClz 控制器类型
     * @param methodName    控制器方法名；方法签名必须是支持的上传签名
     * @param file          待上传文件
     * @return 上传结果
     */
    public static UploadedResult doUpload(Class<?> controllerClz, String methodName, MultipartFile file) {
        return doUpload(controllerClz, methodName, file, null);
    }

    /**
     * 根据控制器方法上的注解上传文件，并允许调用方修改生成的配置。
     *
     * @param controllerClz 控制器类型
     * @param methodName    控制器方法名
     * @param file          待上传文件
     * @param customConfig  配置修改回调；可为 {@code null}
     * @return 上传结果
     */
    public static UploadedResult doUpload(Class<?> controllerClz, String methodName, MultipartFile file, Consumer<FileUploadConfig> customConfig) {
        return doUpload(controllerClz, methodName, file, customConfig, null);
    }

    /**
     * 根据控制器方法上的注解上传文件，并可提供配置修改回调和数据库保存回调。
     * <p>支持的方法签名为 {@code (MultipartFile)} 或
     * {@code (MultipartFile, HttpServletResponse)}。目标方法必须直接声明
     * {@link FileUploadAction}。</p>
     *
     * @param controllerClz 控制器类型
     * @param methodName 控制器方法名
     * @param file 待上传文件
     * @param customConfig 配置修改回调；可为 {@code null}
     * @param saveToDatabase 数据库存储回调；使用
     *                       {@link com.ajaxjs.fileupload.policy.StorageType#DATABASE}
     *                       时必须提供并返回非 {@code null} 结果
     * @return 上传结果
     * @throws UnsupportedOperationException 未找到支持签名的方法或方法没有上传注解时抛出
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
