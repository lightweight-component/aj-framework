package com.ajaxjs.framework.fileupload.policy;

import com.ajaxjs.framework.fileupload.DetectType;
import com.ajaxjs.framework.fileupload.FileUploadConfig;
import com.ajaxjs.util.ObjectHelper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class ContentTypePolicy {
    public enum Policy {
        /**
         * No check content type.
         */
        NO_CHECK(null),

        /**
         * Check if it's in the pass-list.
         */
        WHITELIST(1),

        /**
         * Check if it's with extension name.
         */
        MAPPING(2),

        ALL(3);

        final Integer value;

        Policy(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    final String fileName;

    final String contentType;

    final Policy policy;

    final DetectType detectType;

    public ContentTypePolicy(MultipartFile file, FileUploadConfig config) {
        this.fileName = file.getOriginalFilename();
        this.contentType = file.getContentType();
        this.policy = config.getContentTypePolicy();
        this.detectType = config.getDetectType();
    }

    public void check() {
        Integer value = policy.getValue();

        if ((value & Policy.WHITELIST.value) == Policy.WHITELIST.value)
            simpleCheck();

        if ((value & Policy.MAPPING.value) == Policy.MAPPING.value)
            checkMapping();
    }

    /**
     * Pass-list check of content-type.
     */
    private void simpleCheck() {
        switch (detectType) {
            case NONE:
                break;
            case IMAGE:
                if (!IMAGE_CONTENT_TYPES.contains(contentType))
                    throw new IllegalArgumentException("The uploaded file(" + contentType + ") is not an image.");
                break;
            case OFFICE_FILE:
                if (!OFFICE_CONTENT_TYPES.contains(contentType))
                    throw new IllegalArgumentException("The uploaded file(" + contentType + ") is not an office file.");
                break;
            case AUDIO:
                if (!AUDIO_CONTENT_TYPES.contains(contentType))
                    throw new IllegalArgumentException("The uploaded file(" + contentType + ") is not an audio.");
                break;
            case VIDEO:
                if (!VIDEO_CONTENT_TYPES.contains(contentType))
                    throw new IllegalArgumentException("The uploaded file(" + contentType + ") is not a video.");
                break;
        }
    }

    /**
     * Check content-type by extension name.
     * Beware that it's nothing related to the `DetectType detectType`.
     * Though the content-type is not within `DetectType detectType`, it'll pass.
     */
    private void checkMapping() {
        Path fakePath = Paths.get(fileName);

        try {
            String expectedByExt = Files.probeContentType(fakePath);

            if (!contentType.equals(expectedByExt))
                throw new IllegalArgumentException("The ext type: " + expectedByExt + " doesn't match with: " + contentType);
        } catch (IOException e) {
            throw new UncheckedIOException("checkMapping", e);
        }
    }

    static final Set<String> IMAGE_CONTENT_TYPES = ObjectHelper.setOf(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/webp",
            "image/tiff",
            "image/svg+xml",
            "image/x-icon"
    );

    @SuppressWarnings("SpellCheckingInspection")
    static final Set<String> OFFICE_CONTENT_TYPES = ObjectHelper.setOf(
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.ms-excel", // .xls
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-powerpoint", // .ppt
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
            "application/pdf", // .pdf
            "application/vnd.oasis.opendocument.text", // .odt
            "application/vnd.oasis.opendocument.spreadsheet", // .ods
            "application/vnd.oasis.opendocument.presentation" // .odp
    );

    static final Set<String> AUDIO_CONTENT_TYPES = ObjectHelper.setOf(
            "audio/mpeg",           // .mp3
            "audio/x-wav",          // .wav
            "audio/wav",            // .wav (alternative)
            "audio/wave",           // .wav
            "audio/x-pn-wav",       // .wav (streaming)
            "audio/aac",            // .aac
            "audio/mp4",            // .m4a, .mp4 (audio)
            "audio/x-m4a",          // .m4a
            "audio/ogg",            // .ogg, .oga
            "audio/webm",           // .webm (audio)
            "audio/3gpp",           // .3gp (audio only)
            "audio/3gpp2",          // .3g2 (audio only)
            "audio/amr",            // .amr
            "audio/flac"            // .flac
    );

    @SuppressWarnings("SpellCheckingInspection")
    static final Set<String> VIDEO_CONTENT_TYPES = ObjectHelper.setOf(
            "video/mp4",              // .mp4
            "video/mpeg",             // .mpeg, .mpg
            "video/ogg",              // .ogv
            "video/webm",             // .webm
            "video/quicktime",        // .mov (Apple)
            "video/x-msvideo",        // .avi
            "video/x-ms-wmv",         // .wmv (Windows Media)
            "video/x-flv",            // .flv (Flash, legacy)
            "video/3gpp",             // .3gp (mobile)
            "video/3gpp2",            // .3g2
            "video/x-matroska",       // .mkv
            "video/avi",              // .avi (alternative)
            "video/x-m4v"             // .m4v (MP4 variant)
    );
}
