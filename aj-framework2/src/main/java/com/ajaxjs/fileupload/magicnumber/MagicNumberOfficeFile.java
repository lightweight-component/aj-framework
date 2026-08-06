package com.ajaxjs.fileupload.magicnumber;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 文档文件头及 ZIP Office 容器检测规则。
 */
public class MagicNumberOfficeFile {
    private static final int MAX_ZIP_ENTRIES = 10_000;
    private static final long MAX_UNCOMPRESSED_BYTES = 100L * 1024 * 1024;
    private static final int MAX_MIMETYPE_BYTES = 256;

    private static final Set<String> ZIP_OFFICE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "docx", "xlsx", "pptx", "dotx", "xltx", "potx", "odt", "ods", "odp"
    ));

    /**
     * 非 ZIP 文档扩展名到文件头检测函数的映射。
     */
    public static final Map<String, Function<byte[], Boolean>> OFFICE_MAGIC_MAP = new HashMap<>();

    static {
        // OLE Compound File (DOC, XLS, PPT)
        byte[] OLE_MAGIC = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1};
        Function<byte[], Boolean> oleChecker = bytes -> MagicNumber.startsWith(bytes, OLE_MAGIC);

        OFFICE_MAGIC_MAP.put("doc", oleChecker);
        OFFICE_MAGIC_MAP.put("xls", oleChecker);
        OFFICE_MAGIC_MAP.put("ppt", oleChecker);

        // PDF
        OFFICE_MAGIC_MAP.put("pdf", bytes -> bytes.length >= 4 && bytes[0] == 0x25 && bytes[1] == 0x50 && bytes[2] == 0x44 && bytes[3] == 0x46);

        // RTF
        OFFICE_MAGIC_MAP.put("rtf", bytes -> bytes.length >= 5 && bytes[0] == 0x7B && bytes[1] == 0x5C && bytes[2] == 0x72 && bytes[3] == 0x74 && bytes[4] == 0x66);
    }

    /**
     * 判断扩展名是否属于需要检查 ZIP 内部结构的文档格式。
     *
     * @param ext 不含点的扩展名；可为 {@code null}
     * @return 属于 ZIP Office 或 OpenDocument 格式时返回 {@code true}
     */
    public static boolean isZipOfficeExtension(String ext) {
        return ext != null && ZIP_OFFICE_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT));
    }

    /**
     * 以流式方式校验 ZIP Office 容器，不保留解压后的文件内容。
     * <p>最多处理 10,000 个条目和 100 MiB 累计解压数据；超过限制返回
     * {@code false}。</p>
     *
     * @param input ZIP 文件输入流；方法会关闭该流
     * @param ext   不含点的文档扩展名
     * @return 容器包含对应格式的必要标志时返回 {@code true}
     * @throws IOException ZIP 内容损坏或读取失败时抛出
     */
    public static boolean isValidZipOffice(InputStream input, String ext) throws IOException {
        String normalizedExt = ext.toLowerCase(Locale.ROOT);
        Set<String> entries = new HashSet<>();
        String mimetype = null;
        int entryCount = 0;
        long uncompressedBytes = 0;
        byte[] buffer = new byte[8192];

        try (ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                if (++entryCount > MAX_ZIP_ENTRIES)
                    return false;

                String name = normalizeEntryName(entry.getName());

                if (name == null)
                    return false;

                if (!entry.isDirectory())
                    entries.add(name);
                ByteArrayOutputStream mimetypeContent = "mimetype".equals(name)
                        ? new ByteArrayOutputStream(MAX_MIMETYPE_BYTES) : null;
                int count;

                while ((count = zip.read(buffer)) != -1) {
                    uncompressedBytes += count;

                    if (uncompressedBytes > MAX_UNCOMPRESSED_BYTES)
                        return false;

                    if (mimetypeContent != null) {
                        if (mimetypeContent.size() + count > MAX_MIMETYPE_BYTES)
                            return false;

                        mimetypeContent.write(buffer, 0, count);
                    }
                }

                if (mimetypeContent != null)
                    mimetype = mimetypeContent.toString(StandardCharsets.US_ASCII);
            }
        }

        return hasRequiredEntries(normalizedExt, entries, mimetype);
    }

    private static String normalizeEntryName(String name) {
        if (name == null)
            return null;

        String normalized = name.replace('\\', '/');

        if (normalized.startsWith("/"))
            return null;

        for (String part : normalized.split("/"))
            if ("..".equals(part))
                return null;

        return normalized;
    }

    private static boolean hasRequiredEntries(String ext, Set<String> entries, String mimetype) {
        if ("docx".equals(ext) || "dotx".equals(ext))
            return entries.contains("[Content_Types].xml") && entries.contains("word/document.xml");

        if ("xlsx".equals(ext) || "xltx".equals(ext))
            return entries.contains("[Content_Types].xml") && entries.contains("xl/workbook.xml");

        if ("pptx".equals(ext) || "potx".equals(ext))
            return entries.contains("[Content_Types].xml") && entries.contains("ppt/presentation.xml");

        if (!entries.contains("content.xml"))
            return false;

        if ("odt".equals(ext))
            return "application/vnd.oasis.opendocument.text".equals(mimetype);

        if ("ods".equals(ext))
            return "application/vnd.oasis.opendocument.spreadsheet".equals(mimetype);

        if ("odp".equals(ext))
            return "application/vnd.oasis.opendocument.presentation".equals(mimetype);

        return false;
    }
}
