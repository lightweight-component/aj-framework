package com.ajaxjs.fileupload.policy;

import com.ajaxjs.fileupload.FileUploadConfig;
import com.ajaxjs.util.ObjectHelper;

import java.util.Set;
import java.util.Collections;
import java.util.Locale;

/**
 * Upload file extension validator.
 */
public class ExtensionCheck {
    /**
     * Validates the extension against the custom pass-list and detection category.
     *
     * @param config upload configuration
     * @param ext    extension without leading dot
     * @throws IllegalArgumentException thrown when the extension is not within the allowed range
     */
    public static void checkExtName(FileUploadConfig config, String ext) {
        ext = ext.toLowerCase(Locale.ROOT);
        // 1. simple check by custom ext
        String[] allowExtFilenames = config.getAllowExtFilenames();

        if (!ObjectHelper.isEmpty(allowExtFilenames)) // ignore
            ExtensionCheck.checkExtName(allowExtFilenames, ext);

        // 2. checks by DetectType
        switch (config.getDetectType()) {
            case IMAGE:
                if (!IMAGE_EXTENSIONS.contains(ext))
                    throw new IllegalArgumentException("[ExtCheck]The uploaded file(*." + ext + ") is not an image.");
                break;
            case OFFICE_FILE:
                if (!OFFICE_EXTENSIONS.contains(ext))
                    throw new IllegalArgumentException("[ExtCheck]The uploaded file(*." + ext + ") is not an office file.");
                break;
            case AUDIO:
                if (!AUDIO_EXTENSIONS.contains(ext))
                    throw new IllegalArgumentException("[ExtCheck]The uploaded file(*." + ext + ") is not an audio.");
                break;
            case VIDEO:
                if (!VIDEO_EXTENSIONS.contains(ext))
                    throw new IllegalArgumentException("[ExtCheck]The uploaded file(*." + ext + ") is not a video.");
                break;
        }
    }

    /**
     * Validates the extension against a caller-provided pass-list.
     *
     * @param allowExtFilenames array of allowed extensions, elements without leading dot
     * @param ext               extension to validate
     * @throws IllegalArgumentException thrown when no allowed value matches
     */
    public static void checkExtName(String[] allowExtFilenames, String ext) {
        boolean isFound = false;

        for (String _ext : allowExtFilenames) {
            if (_ext.equalsIgnoreCase(ext)) {
                isFound = true;
                break;
            }
        }

        if (!isFound)
            throw new IllegalArgumentException(ext + " 上传类型不允许上传");
    }

    /**
     * Common image extension set
     */
    static final Set<String> IMAGE_EXTENSIONS = Collections.unmodifiableSet(ObjectHelper.setOf(
            "jpg", "gif", "png", "jpeg", "webp"
    ));

    /* Common office file extensions */

    /**
     * Document extension set supporting category-based initial screening
     */
    public static final Set<String> OFFICE_EXTENSIONS = Collections.unmodifiableSet(ObjectHelper.setOf(
            "dotx", "xltx", "xlsx", "rtf", "docx", "pptx", "pdf", "ppt", "potx",
            "doc", "odp", "xls", "odt", "ods", "md", "wps", "txt"
    ));

    /**
     * Audio extension set supporting category-based initial screening
     */
    public static final Set<String> AUDIO_EXTENSIONS = Collections.unmodifiableSet(ObjectHelper.setOf(
            "mp3",    // MPEG Audio Layer III
            "wav",    // Waveform Audio File Format
            "aac",    // Advanced Audio Coding
            "m4a",    // MPEG-4 Audio (Apple)
            "ogg",    // Ogg Vorbis
            "oga",    // Ogg Audio
            "flac",   // Free Lossless Audio Codec
            "wma",    // Windows Media Audio
            "amr",    // Adaptive Multi-Rate (mobile)
            "aiff",   // Audio Interchange File Format (Apple)
            "au",     // Sun Audio
            "mid",    // MIDI (Musical Instrument Digital Interface)
            "midi",   // MIDI
            "weba",   // WebM Audio (WebM container, audio only)
            "opus",    // Opus audio format (often in .ogg or .opus)
            "caf"
    ));

    /**
     * Video extension set supporting category-based initial screening.
     */
    public static final Set<String> VIDEO_EXTENSIONS = Collections.unmodifiableSet(ObjectHelper.setOf(
            "mp4",      // MPEG-4 Part 14 (most common)
            "avi",      // Audio Video Interleave (Windows)
            "mov",      // Apple QuickTime Movie
            "wmv",      // Windows Media Video
            "flv",      // Flash Video (legacy streaming)
            "mkv",      // Matroska Video (supports multi-track audio/subtitles)
            "webm",     // WebM (HTML5 video, VP8/VP9)
            "mpeg",     // MPEG-1 or MPEG-2 video
            "mpg",      // same as .mpeg
            "m4v",      // MPEG-4 Video (Apple, similar to MP4)
            "3gp",      // 3GPP (mobile video, low bandwidth)
            "3g2",      // 3GPP2 (similar to 3gp)
            "vob",      // DVD Video Object (DVD disc)
            "ogv",      // Ogg Theora Video
            "ts",       // MPEG Transport Stream (live-streaming)
            "f4v",      // Flash MP4 variant
            "rmvb",     // RealMedia Variable Bitrate (legacy Chinese video)
            "asf"       // Advanced Systems Format (Microsoft container)
    ));
}
