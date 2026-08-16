package com.ajaxjs.fileupload.magicnumber;

import com.ajaxjs.fileupload.DetectType;
import com.ajaxjs.fileupload.FileUploadConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Security-focused tests for {@link MagicNumber}, verifying that magic number
 * checks cannot be bypassed by crafted files (e.g., ordinary ZIPs posing as
 * Office documents, or mismatched EBML container types).
 */
class TestMagicNumberSecurity {

    /**
     * Verifies that the magic number check for multipart files does not call
     * {@code getBytes()}, which would risk loading the entire file into memory.
     */
    @Test
    void multipartCheckDoesNotCallGetBytes() {
        byte[] png = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", png) {
            @Override
            public byte[] getBytes() throws IOException {
                throw new AssertionError("getBytes() must not be called");
            }
        };
        FileUploadConfig config = config(DetectType.IMAGE);

        assertDoesNotThrow(() -> MagicNumber.checkMagicNumber(file, config, "png"));
    }

    /**
     * Verifies that an ordinary ZIP file cannot pass the magic number check
     * when claiming to be a DOCX file (which requires specific OOXML entries).
     *
     * @throws Exception if an I/O error occurs
     */
    @Test
    void ordinaryZipCannotPretendToBeDocx() throws Exception {
        MockMultipartFile file = officeFile("document.docx", zip("notes.txt"));

        assertThrows(UnsupportedOperationException.class,
                () -> MagicNumber.checkMagicNumber(file, config(DetectType.OFFICE_FILE), "docx"));
    }

    /**
     * Verifies that a valid DOCX file (containing {@code [Content_Types].xml}
     * and {@code word/document.xml} entries) passes the magic number check.
     *
     * @throws Exception if an I/O error occurs
     */
    @Test
    void docxRequiresContainerMarkers() throws Exception {
        MockMultipartFile file = officeFile(
                "document.docx",
                zip("[Content_Types].xml", "word/document.xml")
        );

        assertDoesNotThrow(
                () -> MagicNumber.checkMagicNumber(file, config(DetectType.OFFICE_FILE), "docx"));
    }

    /**
     * Verifies that MKV and WebM files require matching EBML doc types;
     * a WebM file cannot pass as MKV and vice versa.
     */
    @Test
    void mkvAndWebmRequireMatchingEbmlDocType() {
        byte[] webm = ebml("webm");
        byte[] matroska = ebml("matroska");

        assertDoesNotThrow(() -> MagicNumber.checkMagicNumber(DetectType.VIDEO, webm, "webm"));
        assertDoesNotThrow(() -> MagicNumber.checkMagicNumber(DetectType.VIDEO, matroska, "mkv"));
        assertThrows(UnsupportedOperationException.class,
                () -> MagicNumber.checkMagicNumber(DetectType.VIDEO, webm, "mkv"));
        assertThrows(UnsupportedOperationException.class,
                () -> MagicNumber.checkMagicNumber(DetectType.VIDEO, matroska, "webm"));
    }

    /**
     * Creates a {@link FileUploadConfig} with the given detect type and magic
     * number checking enabled.
     *
     * @param detectType the detect type to set
     * @return a configured {@link FileUploadConfig} instance
     */
    private static FileUploadConfig config(DetectType detectType) {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(detectType);
        config.setCheckMagicNumber(true);
        return config;
    }

    /**
     * Creates a {@link MockMultipartFile} representing an Office file with
     * the given name and ZIP content.
     *
     * @param name    the file name
     * @param content the ZIP content bytes
     * @return a configured {@link MockMultipartFile} instance
     */
    private static MockMultipartFile officeFile(String name, byte[] content) {
        return new MockMultipartFile("file", name, "application/zip", content);
    }

    /**
     * Creates a ZIP file in memory containing the given entry names.
     *
     * @param entries the entry names to include in the ZIP
     * @return the ZIP file content as a byte array
     * @throws IOException if an I/O error occurs
     */
    private static byte[] zip(String... entries) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            for (String name : entries) {
                zip.putNextEntry(new ZipEntry(name));
                zip.write("x".getBytes(StandardCharsets.US_ASCII));
                zip.closeEntry();
            }
        }

        return output.toByteArray();
    }

    /**
     * Creates a minimal EBML byte sequence with the given doc type.
     *
     * @param docType the EBML document type (e.g., "webm" or "matroska")
     * @return the EBML byte sequence
     */
    private static byte[] ebml(String docType) {
        byte[] value = docType.getBytes(StandardCharsets.US_ASCII);
        byte[] bytes = new byte[8 + value.length];
        bytes[0] = 0x1A;
        bytes[1] = 0x45;
        bytes[2] = (byte) 0xDF;
        bytes[3] = (byte) 0xA3;
        bytes[4] = (byte) (0x80 | 3 + value.length);
        bytes[5] = 0x42;
        bytes[6] = (byte) 0x82;
        bytes[7] = (byte) (0x80 | value.length);
        System.arraycopy(value, 0, bytes, 8, value.length);

        return bytes;
    }
}