package com.ajaxjs.fileupload.magicnumber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MagicNumberVideo}, covering {@code readVint}, {@code isFtyp},
 * and {@code readEbmlDocType}.
 */
class TestMagicNumberVideo {

    // ── readVint ──────────────────────────────────────────────────────────

    @Test
    void readVintSingleByte() {
        byte[] bytes = {(byte) 0x80};

        MagicNumberVideo.Vint v = MagicNumberVideo.readVint(bytes, 0, true);
        assertNotNull(v);
        assertEquals(1, v.length);
        assertEquals(0, v.value);

        v = MagicNumberVideo.readVint(bytes, 0, false);
        assertNotNull(v);
        assertEquals(1, v.length);
        assertEquals(0x80, v.value);
    }

    @Test
    void readVintMultiByte() {
        byte[] bytes = {0x40, 0x01};

        MagicNumberVideo.Vint v = MagicNumberVideo.readVint(bytes, 0, true);
        assertNotNull(v);
        assertEquals(2, v.length);
        assertEquals(1, v.value);

        v = MagicNumberVideo.readVint(bytes, 0, false);
        assertNotNull(v);
        assertEquals(2, v.length);
        assertEquals(0x4001, v.value);
    }

    @Test
    void readVintThreeByte() {
        byte[] bytes = {0x20, 0x00, 0x05};

        MagicNumberVideo.Vint v = MagicNumberVideo.readVint(bytes, 0, true);
        assertNotNull(v);
        assertEquals(3, v.length);
        assertEquals(5, v.value);
    }

    @Test
    void readVintOffsetAtEndReturnsNull() {
        assertNull(MagicNumberVideo.readVint(new byte[]{0x01}, 1, true));
    }

    @Test
    void readVintOffsetBeyondReturnsNull() {
        assertNull(MagicNumberVideo.readVint(new byte[]{0x01}, 5, true));
    }

    // ── isFtyp ────────────────────────────────────────────────────────────

    @Test
    void isFtypDetectsMp4Brand() {
        assertTrue(MagicNumberVideo.isFtyp(buildFtyp("mp42"), "mp42"));
    }

    @Test
    void isFtypDetectsM4vBrand() {
        assertTrue(MagicNumberVideo.isFtyp(buildFtyp("M4V "), "M4V "));
    }

    @Test
    void isFtypDetectsQtBrand() {
        assertTrue(MagicNumberVideo.isFtyp(buildFtyp("qt  "), "qt  "));
    }

    @Test
    void isFtypTooShortReturnsFalse() {
        assertFalse(MagicNumberVideo.isFtyp(new byte[11], "mp4"));
    }

    @Test
    void isFtypWrongBrandReturnsFalse() {
        assertFalse(MagicNumberVideo.isFtyp(buildFtyp("mp42"), "M4V "));
    }

    // ── readEbmlDocType ───────────────────────────────────────────────────

    /**
     * Valid EBML header with "webm" DocType.
     * <pre>
     * 1A 45 DF A3  -- EBML magic
     * 87           -- header size VINT (value=7)
     * 42 82        -- DocType element ID (0x4282)
     * 84           -- element size VINT (value=4)
     * 77 65 62 6D  -- "webm"
     * </pre>
     */
    @Test
    void readEbmlDocTypeWebm() {
        byte[] bytes = {
                0x1A, 0x45, (byte) 0xDF, (byte) 0xA3,
                (byte) 0x87,
                0x42, (byte) 0x82,
                (byte) 0x84,
                0x77, 0x65, 0x62, 0x6D
        };
        assertEquals("webm", MagicNumberVideo.readEbmlDocType(bytes));
    }

    @Test
    void readEbmlDocTypeMatroska() {
        byte[] bytes = {
                0x1A, 0x45, (byte) 0xDF, (byte) 0xA3,
                (byte) 0x8B,
                0x42, (byte) 0x82,
                (byte) 0x88,
                0x6D, 0x61, 0x74, 0x72, 0x6F, 0x73, 0x6B, 0x61
        };
        assertEquals("matroska", MagicNumberVideo.readEbmlDocType(bytes));
    }

    @Test
    void readEbmlDocTypeNotEbmlReturnsNull() {
        assertNull(MagicNumberVideo.readEbmlDocType(new byte[]{0, 0, 0, 0}));
    }

    @Test
    void readEbmlDocTypeTruncatedReturnsNull() {
        assertNull(MagicNumberVideo.readEbmlDocType(new byte[]{
                0x1A, 0x45, (byte) 0xDF, (byte) 0xA3
        }));
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private static byte[] buildFtyp(String brand) {
        byte[] bytes = new byte[12];
        bytes[4] = 'f';
        bytes[5] = 't';
        bytes[6] = 'y';
        bytes[7] = 'p';
        bytes[8] = (byte) brand.charAt(0);
        bytes[9] = (byte) brand.charAt(1);
        bytes[10] = (byte) brand.charAt(2);
        bytes[11] = (byte) brand.charAt(3);
        return bytes;
    }
}
