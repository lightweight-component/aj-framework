package com.ajaxjs.framework.fileupload.policy;

import com.ajaxjs.framework.fileupload.FileUploadConfig;
import com.ajaxjs.util.ObjectHelper;

import java.util.Set;

public class ExtensionCheck {
    public static void checkExtName(FileUploadConfig config, String ext) {
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
                    throw new IllegalArgumentException("[ExtCheck]The uploaded file(*." + ext + ") is not an image.");
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

    // 扩展名判断
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
     * Common image file extensions
     */
    static final Set<String> IMAGE_EXTENSIONS = ObjectHelper.setOf(
            "jpg", "gif", "png", "jpeg", "webp"
    );

    /**
     * Common office file extensions
     */
    public static final Set<String> OFFICE_EXTENSIONS = ObjectHelper.setOf(
            "dotx", "xltx", "xlsx", "rtf", "docx", "pptx", "pdf", "ppt", "potx", "doc", "odp", "xls", "odt", "ods"
    );
    public static final Set<String> AUDIO_EXTENSIONS = ObjectHelper.setOf(
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
            "opus"    // Opus audio format (often in .ogg or .opus)
    );
    public static final Set<String> VIDEO_EXTENSIONS = ObjectHelper.setOf(
            "mp4",      // MPEG-4 Part 14 (最通用)
            "avi",      // Audio Video Interleave (Windows)
            "mov",      // Apple QuickTime Movie
            "wmv",      // Windows Media Video
            "flv",      // Flash Video (旧版流媒体)
            "mkv",      // Matroska Video (支持多音轨/字幕)
            "webm",     // WebM (HTML5 视频，VP8/VP9)
            "mpeg",     // MPEG-1 或 MPEG-2 视频
            "mpg",      // 同 .mpeg
            "m4v",      // MPEG-4 Video (Apple, 类似 MP4)
            "3gp",      // 3GPP (手机视频，低带宽)
            "3g2",      // 3GPP2 (类似 3gp)
            "vob",      // DVD Video Object (DVD 光盘)
            "ogv",      // Ogg Theora Video
            "ts",       // MPEG Transport Stream (直播流)
            "f4v",      // Flash MP4 variant
            "rmvb",     // RealMedia Variable Bitrate (国内老视频)
            "asf"       // Advanced Systems Format (微软容器)
    );
}
