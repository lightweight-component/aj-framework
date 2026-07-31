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

class TestMagicNumberSecurity {
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

    @Test
    void ordinaryZipCannotPretendToBeDocx() throws Exception {
        MockMultipartFile file = officeFile("document.docx", zip("notes.txt"));

        assertThrows(UnsupportedOperationException.class,
                () -> MagicNumber.checkMagicNumber(file, config(DetectType.OFFICE_FILE), "docx"));
    }

    @Test
    void docxRequiresContainerMarkers() throws Exception {
        MockMultipartFile file = officeFile(
                "document.docx",
                zip("[Content_Types].xml", "word/document.xml")
        );

        assertDoesNotThrow(
                () -> MagicNumber.checkMagicNumber(file, config(DetectType.OFFICE_FILE), "docx"));
    }

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

    private static FileUploadConfig config(DetectType detectType) {
        FileUploadConfig config = new FileUploadConfig();
        config.setDetectType(detectType);
        config.setCheckMagicNumber(true);
        return config;
    }

    private static MockMultipartFile officeFile(String name, byte[] content) {
        return new MockMultipartFile("file", name, "application/zip", content);
    }

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
