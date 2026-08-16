package com.ajaxjs.fileupload.magicnumber;

import com.ajaxjs.fileupload.DetectType;
import com.ajaxjs.fileupload.FileUploadConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * 根据文件头或容器结构校验上传文件类型。
 * <p>普通格式最多读取文件前 64 KiB；ZIP Office 文件采用有限制的流式校验。</p>
 */
public class MagicNumber {
    static final int MAX_PREFIX_BYTES = 64 * 1024;

    /**
     * 根据上传配置校验 multipart 文件。
     *
     * @param file   上传文件
     * @param config 上传配置
     * @param ext    不含点的文件扩展名
     * @throws UnsupportedOperationException 内容与声明类别不匹配时抛出
     * @throws UncheckedIOException          读取文件失败时抛出
     */
    public static void checkMagicNumber(MultipartFile file, FileUploadConfig config, String ext) {
        if (!config.isCheckMagicNumber())
            return;

        DetectType detectType = config.getDetectType();

        try (InputStream input = file.getInputStream()) {
            if (detectType == DetectType.OFFICE_FILE && MagicNumberOfficeFile.isZipOfficeExtension(ext)) {
                if (!MagicNumberOfficeFile.isValidZipOffice(input, ext))
                    throw new UnsupportedOperationException("[MG Detect]The uploaded file should be an office file.");
            } else
                MagicNumber.checkMagicNumber(detectType, readPrefix(input, MAX_PREFIX_BYTES), ext);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to inspect the uploaded file.", e);
        }
    }

    /**
     * 使用已读取的字节校验文件类型。
     *
     * @param detectType 检测类别
     * @param bytes      固定签名格式的文件前缀；ZIP Office 格式必须传入完整文件内容
     * @param ext        不含点的扩展名
     * @throws UnsupportedOperationException 未匹配对应格式时抛出
     */
    public static void checkMagicNumber(DetectType detectType, byte[] bytes, String ext) {
        switch (detectType) {
            case IMAGE:
                if (!MagicNumber.isValidFile(ext, bytes, MagicNumberImage.IMAGE_MAGIC_MAP))
                    throw new UnsupportedOperationException("[MG Detect]The uploaded file should be a image file.");
                break;
            case OFFICE_FILE:
                if ("txt".equals(ext) || "md".equals(ext)) // txt/md has no magic number
                    return;

                if (MagicNumberOfficeFile.isZipOfficeExtension(ext)) {
                    try {
                        if (!MagicNumberOfficeFile.isValidZipOffice(new ByteArrayInputStream(bytes), ext))
                            throw new UnsupportedOperationException("[MG Detect]The uploaded file should be an office file.");
                    } catch (IOException e) {
                        throw new UncheckedIOException("Failed to inspect the uploaded office file.", e);
                    }
                } else if (!MagicNumber.isValidFile(ext, bytes, MagicNumberOfficeFile.OFFICE_MAGIC_MAP))
                    throw new UnsupportedOperationException("[MG Detect]The uploaded file should be an office file.");
                break;
            case AUDIO:
                if (!MagicNumber.isValidFile(ext, bytes, MagicNumberAudio.AUDIO_MAGIC_MAP))
                    throw new UnsupportedOperationException("[MG Detect]The uploaded file should be a audio file.");
                break;
            case VIDEO:
                if (!MagicNumber.isValidFile(ext, bytes, MagicNumberVideo.VIDEO_MAGIC_MAP))
                    throw new UnsupportedOperationException("[MG Detect]The uploaded file should be a video file.");
                break;
        }
    }

    static byte[] readPrefix(InputStream input, int limit) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(limit, 8192));
        byte[] buffer = new byte[4096];
        int remaining = limit;

        while (remaining > 0) {
            int count = input.read(buffer, 0, Math.min(buffer.length, remaining));

            if (count == -1)
                break;

            output.write(buffer, 0, count);
            remaining -= count;
        }

        return output.toByteArray();
    }

    /**
     * 判断字节数组是否以指定前缀开始。
     *
     * @param data   待检查数据
     * @param prefix 期望前缀
     * @return 匹配返回 {@code true}
     */
    public static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length)
            return false;

        for (int i = 0; i < prefix.length; i++)
            if (data[i] != prefix[i])
                return false;

        return true;
    }

    /**
     * 使用扩展名对应的检测函数校验内容。
     *
     * @param ext      不含点的扩展名
     * @param bytes    待检测字节
     * @param magicMap 扩展名到检测函数的映射
     * @return 找到检测器且内容匹配时返回 {@code true}
     */
    public static boolean isValidFile(String ext, byte[] bytes, Map<String, Function<byte[], Boolean>> magicMap) {
        Function<byte[], Boolean> validator = magicMap.get(ext.toLowerCase(Locale.ROOT));

        return validator != null && validator.apply(bytes);
    }
}
