package com.ajaxjs.fileupload.magicnumber;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 视频容器文件头检测规则集合。
 */
public class MagicNumberVideo {
    /** 扩展名到视频文件头或容器检测函数的映射。 */
    public static final Map<String, Function<byte[], Boolean>> VIDEO_MAGIC_MAP = new HashMap<>();

    static {
        // MP4 / M4V / M4A / MOV: ftyp....
        VIDEO_MAGIC_MAP.put("mp4", bytes -> isFtyp(bytes, "mp4"));
        VIDEO_MAGIC_MAP.put("m4v", bytes -> isFtyp(bytes, "M4V"));
        VIDEO_MAGIC_MAP.put("mov", bytes -> isFtyp(bytes, "qt  "));

        // MKV / WebM: EBML header with a matching DocType element
        VIDEO_MAGIC_MAP.put("mkv", bytes -> "matroska".equals(readEbmlDocType(bytes)));
        VIDEO_MAGIC_MAP.put("webm", bytes -> "webm".equals(readEbmlDocType(bytes)));

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

    static String readEbmlDocType(byte[] bytes) {
        byte[] ebmlHeader = {(byte) 0x1A, (byte) 0x45, (byte) 0xDF, (byte) 0xA3};

        if (!MagicNumber.startsWith(bytes, ebmlHeader))
            return null;

        Vint headerSize = readVint(bytes, ebmlHeader.length, true);

        if (headerSize == null || headerSize.value > Integer.MAX_VALUE)
            return null;

        int offset = ebmlHeader.length + headerSize.length;
        long calculatedEnd = (long) offset + headerSize.value;

        if (calculatedEnd > bytes.length)
            return null;

        int headerEnd = (int) calculatedEnd;

        while (offset < headerEnd) {
            Vint elementId = readVint(bytes, offset, false);

            if (elementId == null)
                return null;

            offset += elementId.length;
            Vint elementSize = readVint(bytes, offset, true);

            if (elementSize == null || elementSize.value > Integer.MAX_VALUE)
                return null;

            offset += elementSize.length;
            long elementEnd = (long) offset + elementSize.value;

            if (elementEnd > headerEnd)
                return null;

            if (elementId.value == 0x4282)
                return new String(bytes, offset, (int) elementSize.value, StandardCharsets.US_ASCII);

            offset = (int) elementEnd;
        }

        return null;
    }

    private static Vint readVint(byte[] bytes, int offset, boolean removeMarker) {
        if (offset >= bytes.length)
            return null;

        int first = bytes[offset] & 0xFF;
        int mask = 0x80;
        int length = 1;

        while (length <= 8 && (first & mask) == 0) {
            mask >>= 1;
            length++;
        }

        if (length > 8 || offset + length > bytes.length)
            return null;

        long value = removeMarker ? first & (mask - 1) : first;

        for (int i = 1; i < length; i++)
            value = (value << 8) | (bytes[offset + i] & 0xFF);

        return new Vint(length, value);
    }

    private static class Vint {
        final int length;
        final long value;

        Vint(int length, long value) {
            this.length = length;
            this.value = value;
        }
    }
}
