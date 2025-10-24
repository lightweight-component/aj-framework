package com.ajaxjs.framework.fileupload.tools;

import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for detecting file types based on magic numbers.
 */
public class MagicNumber {
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
