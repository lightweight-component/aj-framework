package com.ajaxjs.fileupload.magicnumber;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MagicNumberOfficeFile} covering ZIP entry name normalization
 * and required-entry validation for Office and OpenDocument formats.
 */
class TestMagicNumberOfficeFile {

    // --- normalizeEntryName() normal cases ---

    @Test
    void normalizeEntryNameSimpleNameReturnsUnchanged() {
        assertEquals("word/document.xml", MagicNumberOfficeFile.normalizeEntryName("word/document.xml"));
    }

    @Test
    void normalizeEntryNameConvertsBackslashToForwardSlash() {
        assertEquals("word/document.xml", MagicNumberOfficeFile.normalizeEntryName("word\\document.xml"));
    }

    // --- normalizeEntryName() edge cases ---

    @Test
    void normalizeEntryNameNullReturnsNull() {
        assertNull(MagicNumberOfficeFile.normalizeEntryName(null));
    }

    @Test
    void normalizeEntryNameStartsWithSlashReturnsNull() {
        assertNull(MagicNumberOfficeFile.normalizeEntryName("/word/document.xml"));
    }

    @Test
    void normalizeEntryNameContainsDotDotReturnsNull() {
        assertNull(MagicNumberOfficeFile.normalizeEntryName("word/../document.xml"));
    }

    // --- hasRequiredEntries() valid OOXML ---

    @Test
    void hasRequiredEntriesValidDocx() {
        Set<String> entries = new HashSet<>();
        entries.add("[Content_Types].xml");
        entries.add("word/document.xml");

        assertTrue(MagicNumberOfficeFile.hasRequiredEntries("docx", entries, null));
        assertTrue(MagicNumberOfficeFile.hasRequiredEntries("dotx", entries, null));
    }

    @Test
    void hasRequiredEntriesValidXlsx() {
        Set<String> entries = new HashSet<>();
        entries.add("[Content_Types].xml");
        entries.add("xl/workbook.xml");

        assertTrue(MagicNumberOfficeFile.hasRequiredEntries("xlsx", entries, null));
        assertTrue(MagicNumberOfficeFile.hasRequiredEntries("xltx", entries, null));
    }

    @Test
    void hasRequiredEntriesValidPptx() {
        Set<String> entries = new HashSet<>();
        entries.add("[Content_Types].xml");
        entries.add("ppt/presentation.xml");

        assertTrue(MagicNumberOfficeFile.hasRequiredEntries("pptx", entries, null));
        assertTrue(MagicNumberOfficeFile.hasRequiredEntries("potx", entries, null));
    }

    // --- hasRequiredEntries() valid ODF ---

    @Test
    void hasRequiredEntriesValidOdt() {
        Set<String> entries = new HashSet<>();
        entries.add("content.xml");

        assertTrue(MagicNumberOfficeFile.hasRequiredEntries(
                "odt", entries, "application/vnd.oasis.opendocument.text"));
    }

    @Test
    void hasRequiredEntriesValidOds() {
        Set<String> entries = new HashSet<>();
        entries.add("content.xml");

        assertTrue(MagicNumberOfficeFile.hasRequiredEntries(
                "ods", entries, "application/vnd.oasis.opendocument.spreadsheet"));
    }

    @Test
    void hasRequiredEntriesValidOdp() {
        Set<String> entries = new HashSet<>();
        entries.add("content.xml");

        assertTrue(MagicNumberOfficeFile.hasRequiredEntries(
                "odp", entries, "application/vnd.oasis.opendocument.presentation"));
    }

    // --- hasRequiredEntries() invalid ---

    @Test
    void hasRequiredEntriesDocxMissingRequiredEntryReturnsFalse() {
        Set<String> entries = new HashSet<>();
        entries.add("[Content_Types].xml");
        // missing word/document.xml

        assertFalse(MagicNumberOfficeFile.hasRequiredEntries("docx", entries, null));
    }

    @Test
    void hasRequiredEntriesOdtMissingContentXmlReturnsFalse() {
        Set<String> entries = new HashSet<>();
        // no content.xml

        assertFalse(MagicNumberOfficeFile.hasRequiredEntries(
                "odt", entries, "application/vnd.oasis.opendocument.text"));
    }

    @Test
    void hasRequiredEntriesUnknownExtensionReturnsFalse() {
        Set<String> entries = new HashSet<>();
        entries.add("[Content_Types].xml");
        entries.add("word/document.xml");

        assertFalse(MagicNumberOfficeFile.hasRequiredEntries("unknown", entries, null));
    }
}