package com.ajaxjs.fileupload.magicnumber;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Document file header and ZIP Office container detection rules.
 */
public class MagicNumberOfficeFile {
    /**
     * Maximum number of ZIP entries to process before aborting validation.
     */
    private static final int MAX_ZIP_ENTRIES = 10_000;
    /**
     * Maximum cumulative uncompressed bytes to read before aborting validation (100 MiB).
     */
    private static final long MAX_UNCOMPRESSED_BYTES = 100L * 1024 * 1024;
    /**
     * Maximum bytes to read from the mimetype entry.
     */
    private static final int MAX_MIMETYPE_BYTES = 256;

    /**
     * Set of file extensions that correspond to ZIP-based Office or OpenDocument formats.
     */
    private static final Set<String> ZIP_OFFICE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "docx", "xlsx", "pptx", "dotx", "xltx", "potx", "odt", "ods", "odp"
    ));

    /**
     * Map from a non-ZIP document extension to file header detection function.
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
     * Check whether the extension belongs to a document format that requires ZIP internal structure inspection.
     *
     * @param ext Extension without the dot; may be {@code null}
     * @return {@code true} if it is a ZIP Office or OpenDocument format
     */
    public static boolean isZipOfficeExtension(String ext) {
        return ext != null && ZIP_OFFICE_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT));
    }

    /**
     * Validate ZIP Office container in a streaming manner, without retaining decompressed file content.
     * <p>Processes at most 10,000 entries and 100 MiB of cumulative decompressed data; returns
     * {@code false} if limits are exceeded.</p>
     *
     * @param input ZIP file input stream; this method will close the stream
     * @param ext   Document extension without the dot
     * @return {@code true} if the container contains the required markers for the corresponding format
     * @throws IOException Thrown when ZIP content is corrupted or reading fails
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

    /**
     * Normalize a ZIP entry name by converting backslashes to forward slashes and rejecting
     * paths that start with "/" or contain ".." traversal segments.
     *
     * @param name The raw entry name from the ZIP file
     * @return The normalized entry name, or {@code null} if the path is suspicious
     */
    static String normalizeEntryName(String name) {
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

    /**
     * Check whether the set of ZIP entries and optional mimetype content confirm the expected
     * Office or OpenDocument format.
     *
     * @param ext      Normalized document extension (lowercase, no dot)
     * @param entries  Set of entry names found in the ZIP container
     * @param mimetype Content of the mimetype entry, or {@code null} if not present
     * @return {@code true} if the entries match the expected structure for the given extension
     */
    static boolean hasRequiredEntries(String ext, Set<String> entries, String mimetype) {
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
