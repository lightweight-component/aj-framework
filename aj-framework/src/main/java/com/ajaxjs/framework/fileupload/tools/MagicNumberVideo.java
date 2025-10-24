package com.ajaxjs.framework.fileupload.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Detects file type based on magic numbers.
 */
public class MagicNumberVideo {
    public static final Map<String, Function<byte[], Boolean>> VIDEO_MAGIC_MAP = new HashMap<>();

    static {
        // MP4 / M4V / M4A / MOV: ftyp....
        VIDEO_MAGIC_MAP.put("mp4", bytes -> isFtyp(bytes, "mp4"));
        VIDEO_MAGIC_MAP.put("m4v", bytes -> isFtyp(bytes, "M4V"));
        VIDEO_MAGIC_MAP.put("mov", bytes -> isFtyp(bytes, "qt  "));

        // MKV / WebM: EBML header
        byte[] mkvSig = {(byte) 0x1A, (byte) 0x45, (byte) 0xDF, (byte) 0xA3};
        Function<byte[], Boolean> mkvChecker = bytes -> MagicNumber.startsWith(bytes, mkvSig);
        VIDEO_MAGIC_MAP.put("mkv", mkvChecker);
        VIDEO_MAGIC_MAP.put("webm", mkvChecker);

        // AVI: RIFF....AVI
        VIDEO_MAGIC_MAP.put("avi", bytes -> bytes.length >= 12 &&
                bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' &&
                bytes[8] == 'A' && bytes[9] == 'V' && bytes[10] == 'I' && bytes[11] == ' '
        );

        // FLV: FLV header
        VIDEO_MAGIC_MAP.put("flv", bytes -> bytes.length >= 3 && bytes[0] == 'F' && bytes[1] == 'L' && bytes[2] == 'V');
    }

    private static boolean isFtyp(byte[] bytes, String brand) {
        if (bytes.length < 12)
            return false;

        return bytes[4] == 'f' && bytes[5] == 't' && bytes[6] == 'y' && bytes[7] == 'p' &&
                bytes[8] == brand.charAt(0) &&
                bytes[9] == brand.charAt(1) &&
                bytes[10] == brand.charAt(2) &&
                bytes[11] == brand.charAt(3);
    }
}
