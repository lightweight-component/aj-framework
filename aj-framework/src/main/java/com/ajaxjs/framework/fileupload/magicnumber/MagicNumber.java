package com.ajaxjs.framework.fileupload.magicnumber;

import com.ajaxjs.framework.fileupload.DetectType;
import com.ajaxjs.framework.fileupload.FileUploadConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for detecting file types based on magic numbers.
 */
public class MagicNumber {
    public static void checkMagicNumber(MultipartFile file, FileUploadConfig config, String ext) {
        DetectType detectType = config.getDetectType();

        if (config.isCheckMagicNumber()) {
            try {
                MagicNumber.checkMagicNumber(detectType, file.getBytes(), ext);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public static void checkMagicNumber(DetectType detectType, byte[] bytes, String ext) {
        switch (detectType) {
            case IMAGE:
                if (!MagicNumber.isValidFile(ext, bytes, MagicNumberImage.IMAGE_MAGIC_MAP))
                    throw new UnsupportedOperationException("[MG Detect]The uploaded file should be a image file.");
                break;
            case OFFICE_FILE:
                if (!MagicNumber.isValidFile(ext, bytes, MagicNumberOfficeFile.OFFICE_MAGIC_MAP))
                    throw new UnsupportedOperationException("[MG Detect]The uploaded file should be a office file.");
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

    public static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length)
            return false;

        for (int i = 0; i < prefix.length; i++)
            if (data[i] != prefix[i])
                return false;

        return true;
    }

    /**
     * Validates file content against known magic numbers.
     *
     * @param ext   File extension (e.g. "docx", "pdf")
     * @param bytes First 15+ bytes of the file
     * @return true if magic number matches
     */
    public static boolean isValidFile(String ext, byte[] bytes, Map<String, Function<byte[], Boolean>> magicMap) {
        Function<byte[], Boolean> validator = magicMap.get(ext.toLowerCase());

        return validator != null && validator.apply(bytes);
    }
}
