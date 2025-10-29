package com.ajaxjs.framework.fileupload.magicnumber;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Detects file type based on magic numbers.
 */
public class MagicNumberImage {
    public static final Map<String, Function<byte[], Boolean>> IMAGE_MAGIC_MAP = new HashMap<>();

    static {
        // JPEG: FF D8
        IMAGE_MAGIC_MAP.put("jpg", bytes -> bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8);

        IMAGE_MAGIC_MAP.put("jpeg", IMAGE_MAGIC_MAP.get("jpg"));

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        IMAGE_MAGIC_MAP.put("png", bytes -> bytes.length >= 8 && (bytes[0] & 0xFF) == 0x89 &&
                bytes[1] == 0x50 &&
                bytes[2] == 0x4E &&
                bytes[3] == 0x47 &&
                bytes[4] == 0x0D &&
                bytes[5] == 0x0A &&
                bytes[6] == 0x1A &&
                bytes[7] == 0x0A
        );

        // GIF: GIF87a or GIF89a
        IMAGE_MAGIC_MAP.put("gif", bytes ->
                bytes.length >= 6 &&
                        (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8' &&
                                (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a')
        );

        // WEBP: RIFF....WEBP
        IMAGE_MAGIC_MAP.put("webp", bytes ->
                bytes.length >= 12 &&
                        bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' &&
                        bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P'
        );
    }
}
