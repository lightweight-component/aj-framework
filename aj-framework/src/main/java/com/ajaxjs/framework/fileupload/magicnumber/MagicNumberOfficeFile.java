package com.ajaxjs.framework.fileupload.magicnumber;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Detects file type based on magic numbers.
 */
public class MagicNumberOfficeFile {
    public static final Map<String, Function<byte[], Boolean>> OFFICE_MAGIC_MAP = new HashMap<>();

    static {
        // OLE Compound File (DOC, XLS, PPT)
        byte[] OLE_MAGIC = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1};
        Function<byte[], Boolean> oleChecker = bytes -> MagicNumber.startsWith(bytes, OLE_MAGIC);

        OFFICE_MAGIC_MAP.put("doc", oleChecker);
        OFFICE_MAGIC_MAP.put("xls", oleChecker);
        OFFICE_MAGIC_MAP.put("ppt", oleChecker);

        // ZIP-based OpenXML (DOCX, XLSX, PPTX, etc.)
        Function<byte[], Boolean> zipChecker = bytes -> bytes.length >= 4 && bytes[0] == 0x50 && bytes[1] == 0x4B && bytes[2] == 0x03 && bytes[3] == 0x04;

        List<String> zipFormats = Arrays.asList("docx", "xlsx", "pptx", "dotx", "xltx", "potx", "odt", "ods", "odp");
        zipFormats.forEach(ext -> OFFICE_MAGIC_MAP.put(ext, zipChecker));

        // PDF
        OFFICE_MAGIC_MAP.put("pdf", bytes -> bytes.length >= 4 && bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46);

        // RTF
        OFFICE_MAGIC_MAP.put("rtf", bytes -> bytes.length >= 5 && bytes[0] == 0x7B && bytes[1] == 0x5C && bytes[2] == 0x72 && bytes[3] == 0x74 && bytes[4] == 0x66);
    }
}
