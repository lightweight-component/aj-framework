package com.ajaxjs.framework.fileupload.tools;

import com.ajaxjs.framework.fileupload.FileUploadConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Data
@RequiredArgsConstructor
public class Detect {
    private final FileUploadConfig config;

    private final MultipartFile file;

    public void check() {
        if (file.isEmpty())
            throw new IllegalArgumentException("没有上传任何文件");

        if (file.getSize() > (config.getMaxFileSize() * 1024 * 1024)) // MB 转字节
            throw new IllegalArgumentException("文件大小超过系统限制！");

        String ext = getExtension(file);

        checkExtName(ext);
        checkMagicNumber(ext);
    }

    private void checkMagicNumber(String ext) {
        DetectType detectType = config.getDetectType();

        if (detectType == null) // ignore
            return;

        try {
            byte[] bytes = file.getBytes();

            switch (detectType) {
                case IMAGE:
                    if (!MagicNumber.isValidFile(ext, bytes, MagicNumberImage.IMAGE_MAGIC_MAP))
                        throw new UnsupportedOperationException("The uploaded file should be a image file.");
                    break;
                case OFFICE_FILE:
                    if (!MagicNumber.isValidFile(ext, bytes, MagicNumberOfficeFile.OFFICE_MAGIC_MAP))
                        throw new UnsupportedOperationException("The uploaded file should be a office file.");
                    break;
                case AUDIO:
                    if (!MagicNumber.isValidFile(ext, bytes, MagicNumberAudio.AUDIO_MAGIC_MAP))
                        throw new UnsupportedOperationException("The uploaded file should be a audio file.");
                    break;
                case VIDEO:
                    if (!MagicNumber.isValidFile(ext, bytes, MagicNumberVideo.VIDEO_MAGIC_MAP))
                        throw new UnsupportedOperationException("The uploaded file should be a video file.");
                    break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 扩展名判断
    private void checkExtName(String ext) {
        String[] allowExtFilenames = config.getAllowExtFilenames();

        if (allowExtFilenames == null || allowExtFilenames.length == 0) // ignore
            return;

        String[] arr = Objects.requireNonNull(file.getOriginalFilename()).split("\\.");
        boolean isFound = false;

        for (String _ext : allowExtFilenames) {
            if (_ext.equalsIgnoreCase(ext)) {
                isFound = true;
                break;
            }
        }

        if (!isFound)
            throw new IllegalArgumentException(ext + " 上传类型不允许上传");
    }


    public static String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename();

        if (name == null || !name.contains("."))
            throw new IllegalArgumentException("The file uploaded doesn't hava a extension name.");

        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Common image file extensions
     */
    public static final String[] IMAGE_EXTENSIONS = {
            "jpg", "gif", "png", "jpeg", "webp"
    };

    /**
     * Common office file extensions
     */
    public static final String[] OFFICE_EXTENSIONS = {
            "dotx", "xltx", "xlsx", "rtf", "docx", "pptx", "pdf", "ppt", "potx", "doc", "odp", "xls", "odt", "ods"
    };

}
