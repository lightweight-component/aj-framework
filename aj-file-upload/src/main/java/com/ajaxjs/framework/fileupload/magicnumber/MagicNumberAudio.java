package com.ajaxjs.framework.fileupload.magicnumber;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Detects file type based on magic numbers.
 */
public class MagicNumberAudio {
    public static final Map<String, Function<byte[], Boolean>> AUDIO_MAGIC_MAP = new HashMap<>();

    static {
        // MP3: ID3 tag or MPEG frame
        AUDIO_MAGIC_MAP.put("mp3", bytes ->
                bytes.length >= 3 &&
                        ((bytes[0] == 'I' && bytes[1] == 'D' && bytes[2] == '3') || // ID3v2
                                ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xE0) == 0xE0))  // MPEG frame sync
        );

        // WAV: RIFF....WAVE
        AUDIO_MAGIC_MAP.put("wav", bytes ->
                bytes.length >= 12 &&
                        bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' &&
                        bytes[8] == 'W' && bytes[9] == 'A' && bytes[10] == 'V' && bytes[11] == 'E'
        );

        // FLAC: fLaC
        AUDIO_MAGIC_MAP.put("flac", bytes ->
                bytes.length >= 4 &&
                        bytes[0] == 'f' && bytes[1] == 'L' && bytes[2] == 'a' && bytes[3] == 'C'
        );

        // AAC: ADTS header (0xFF F1 or 0xFF F9)
        AUDIO_MAGIC_MAP.put("aac", bytes ->
                bytes.length >= 2 &&
                        (bytes[0] & 0xFF) == 0xFF &&
                        ((bytes[1] & 0xFF) == 0xF1 || (bytes[1] & 0xFF) == 0xF9)
        );

        // OGG: OggS
        AUDIO_MAGIC_MAP.put("ogg", bytes ->
                bytes.length >= 4 &&
                        bytes[0] == 'O' && bytes[1] == 'g' && bytes[2] == 'g' && bytes[3] == 'S'
        );

        // M4A (MP4 container): starts with ftypM4A or ftypisom
        AUDIO_MAGIC_MAP.put("m4a", bytes ->
                bytes.length >= 12 &&
                        bytes[4] == 'f' && bytes[5] == 't' && bytes[6] == 'y' && bytes[7] == 'p' &&
                        (bytes[8] == 'M' || bytes[8] == 'i') &&
                        (bytes[9] == '4' || bytes[9] == 's') &&
                        (bytes[10] == 'A' || bytes[10] == 'o')
        );
    }
}