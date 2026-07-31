package com.ajaxjs.fileupload.magicnumber;

import com.ajaxjs.fileupload.DetectType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestMagicNumberBasics {
    @Test
    void startsWithHandlesMatchMismatchAndShortInput() {
        assertTrue(MagicNumber.startsWith(new byte[]{1, 2, 3}, new byte[]{1, 2}));
        assertFalse(MagicNumber.startsWith(new byte[]{1, 3}, new byte[]{1, 2}));
        assertFalse(MagicNumber.startsWith(new byte[]{1}, new byte[]{1, 2}));
        assertTrue(MagicNumber.startsWith(new byte[]{1}, new byte[0]));
    }

    @Test
    void imageSignaturesAcceptKnownHeaders() {
        byte[] png = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8};

        assertTrue(MagicNumber.isValidFile("PNG", png, MagicNumberImage.IMAGE_MAGIC_MAP));
        assertTrue(MagicNumber.isValidFile("jpeg", jpeg, MagicNumberImage.IMAGE_MAGIC_MAP));
        assertFalse(MagicNumber.isValidFile("gif", png, MagicNumberImage.IMAGE_MAGIC_MAP));
    }

    @Test
    void audioSignaturesAcceptKnownHeaders() {
        byte[] mp3 = {'I', 'D', '3'};
        byte[] wave = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'A', 'V', 'E'};

        assertTrue(MagicNumber.isValidFile("mp3", mp3, MagicNumberAudio.AUDIO_MAGIC_MAP));
        assertTrue(MagicNumber.isValidFile("wav", wave, MagicNumberAudio.AUDIO_MAGIC_MAP));
        assertFalse(MagicNumber.isValidFile("flac", wave, MagicNumberAudio.AUDIO_MAGIC_MAP));
    }

    @Test
    void unknownExtensionHasNoValidator() {
        Map<String, Function<byte[], Boolean>> validators = new HashMap<>();
        validators.put("known", bytes -> true);

        assertFalse(MagicNumber.isValidFile("unknown", new byte[]{1}, validators));
    }

    @Test
    void noneDetectionDoesNotRejectContent() {
        assertDoesNotThrow(
                () -> MagicNumber.checkMagicNumber(DetectType.NONE, new byte[0], "unknown")
        );
    }

    @Test
    void knownCategoryRejectsUnknownExtension() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> MagicNumber.checkMagicNumber(DetectType.IMAGE, new byte[]{1}, "unknown")
        );
    }
}
