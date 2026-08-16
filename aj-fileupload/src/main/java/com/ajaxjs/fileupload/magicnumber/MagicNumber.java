package com.ajaxjs.fileupload.magicnumber;

import com.ajaxjs.fileupload.DetectType;
import com.ajaxjs.fileupload.FileUploadConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Validate an uploaded file type based on file header or container structure.
 * <p>For regular formats, reads at most the first 64 KiB of the file; ZIP Office files use bounded streaming validation.</p>
 */
public class MagicNumber {
    /**
     * Maximum number of bytes to read from the file prefix for signature detection (64 KiB).
     */
    static final int MAX_PREFIX_BYTES = 64 * 1024;

    /**
     * Validate a multipart file based on upload configuration.
     *
     * @param file   Uploaded file
     * @param config Upload configuration
     * @param ext    File extension without the dot
     * @throws UnsupportedOperationException Thrown when content does not match the declared type
     * @throws UncheckedIOException          Thrown when reading the file fails
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
     * Validate file type using pre-read bytes.
     *
     * @param detectType Detection category
     * @param bytes      File prefix for fixed signature formats; ZIP Office formats must pass the full file content
     * @param ext        Extension without the dot
     * @throws UnsupportedOperationException Thrown when no matching format is found
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

    /**
     * Read up to the specified number of bytes from the beginning of an input stream.
     *
     * @param input The input stream to read from
     * @param limit Maximum number of bytes to read
     * @return The bytes read from the stream prefix
     * @throws IOException Thrown when reading from the stream fails
     */
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
     * Check whether a byte array starts with a given prefix.
     *
     * @param data   Data to check
     * @param prefix Expected prefix
     * @return {@code true} if matched
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
     * Validate content using the detection function mapped to the extension.
     *
     * @param ext      Extension without the dot
     * @param bytes    Bytes to inspect
     * @param magicMap Map from extension-to-detection function
     * @return {@code true} if a validator is found and the content matches
     */
    public static boolean isValidFile(String ext, byte[] bytes, Map<String, Function<byte[], Boolean>> magicMap) {
        Function<byte[], Boolean> validator = magicMap.get(ext.toLowerCase(Locale.ROOT));

        return validator != null && validator.apply(bytes);
    }
}
