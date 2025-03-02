package com.ajaxjs.embeded_tomcat.filter;


import com.ajaxjs.util.StrUtil;
import com.ajaxjs.util.io.FileHelper;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 文件上传的辅助类
 */
public class FileUploadHelper {
    /**
     * 初始化文件上传功能。
     * 该方法用于配置文件上传的相关设置，包括最大文件大小和临时文件存储路径。
     * 它通过调用另一个重载的 initUpload 方法来实现具体的初始化逻辑。
     *
     * @param cxt          Servlet 上下文，用于获取和设置与 Servlet 相关的配置信息
     * @param registration Servlet 注册对象，用于配置 Servlet 的参数，如最大文件大小
     */
    public static void initUpload(ServletContext cxt, ServletRegistration.Dynamic registration) {
        // 使用系统默认的临时目录和指定的子目录来初始化上传目录
        initUpload(cxt, registration, System.getProperty("java.io.tmpdir") + "upload_dir");
    }

    /**
     * 初始化文件上传配置。
     * 此方法用于配置 Servlet 以支持文件上传功能，它设置了上传目录以及相关的限制参数。
     *
     * @param cxt          Servlet 上下文，用于获取或设置与 Servlet 相关的配置信息。
     * @param registration Servlet 注册对象，用于设置 Servlet 的配置信息，包括 Multipart 配置
     * @param uploadDir    文件上传的目标目录，新上传的文件将保存在该目录下
     */
    public static void initUpload(ServletContext cxt, ServletRegistration.Dynamic registration, String uploadDir) {
        // 创建上传目录，如果目录不存在的话
        FileHelper.mkDir(uploadDir);

        // 设置 Multipart 配置，包括上传目录和限制参数
        registration.setMultipartConfig(new MultipartConfigElement(uploadDir, 50000000, 50000000, 0));// 文件上传
    }

    /**
     * 配置 MultipartResolver，用于处理 HTTP 多部分请求（例如文件上传）。
     * 使用 StandardServletMultipartResolver 作为默认的解析器，并启用延迟解析。
     * 延迟解析的目的是允许在解析实际文件之前进行一些自定义处理，比如验证文件大小。
     *
     * @return 标准的 Servlet 多部分解析器实例，配置为延迟解析
     */
    public static MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        resolver.setResolveLazily(true);// resolveLazily 属性启用是为了推迟文件解析，以在在 UploadAction 中捕获文件大小异常

        return resolver;
    }

    /**
     * 内容格式
     */
    public static final String CONTENT_TYPE = "multipart/form-data";

    /**
     * 内容格式（指定 UTF-8）
     */
    public static final String CONTENT_TYPE2 = "multipart/form-data;charset=UTF-8";

    /**
     * 保存上传的文件
     *
     * @param file          文件
     * @param uploadDir     保存目录
     * @param isNewAutoName 是否重新命名？
     * @return 上传的文件名
     */
    public static String upload(MultipartFile file, String uploadDir, boolean isNewAutoName) {
        Objects.requireNonNull(file);
        String filename = file.getOriginalFilename();

        if (filename == null)
            throw new IllegalArgumentException("表单上传的参数 name 与方法中 MultipartFile 的参数名是否一致?");

        if (isNewAutoName)
            filename = getAutoName(filename);

        FileHelper.mkDir(uploadDir);
        File file2 = new File(uploadDir + filename);

        try {
            file.transferTo(file2);
            boolean v;
            v = file2.setReadable(true, false);
            v = file2.setExecutable(true, false);
            v = file2.setWritable(true, false);
        } catch (IllegalStateException | IOException e) {
            System.err.println("文件上传失败");
            e.printStackTrace();
        }

        return filename;
    }

    /**
     * 根据原始文件名生成自动名称，保留扩展名
     *
     * @param originalFilename 原始文件名
     * @return 自动文件名
     */
    public static String getAutoName(String originalFilename) {
        String[] arr = originalFilename.split("\\.");
        String ext = "";

        if (arr.length >= 2)
            ext = "." + arr[arr.length - 1];
//        else {
//            // 没有扩展名
//        }

//        return SnowflakeId.get() + ext;
        return StrUtil.getRandomString(8) + ext;
    }
}