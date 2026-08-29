package com.ajaxjs.fileupload.policy;

import com.ajaxjs.fileupload.DetectType;
import com.ajaxjs.fileupload.FileUploadConfig;
import com.ajaxjs.util.ObjectHelper;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Performs media type validation based on the declared Content-Type and file extension.
 * <p>The Content-Type in a multipart request is provided by the client and should only serve as a secondary check.</p>
 */
public class ContentTypePolicy {
    /**
     * Content-Type validation modes.
     */
    public enum Policy {
        /**
         * Do not check Content-Type.
         */
        NO_CHECK(null),

        /**
         * Checks whether the Content-Type belongs to the allowed set for the selected file category.
         */
        WHITELIST(1),

        /**
         * Checks whether the Content-Type matches the extension mapping.
         * <p>The expected type is determined by the current operating system's file-extension mapping.</p>
         */
        MAPPING(2),

        /**
         * Performs both pass-list and extension mapping checks.
         */
        ALL(3);

        /**
         * The bit flag value of this policy.
         */
        final Integer value;

        /**
         * Constructs a policy with the given bit flag value.
         *
         * @param value the bit flag value
         */
        Policy(Integer value) {
            this.value = value;
        }

        /**
         * Returns the policy bit flag.
         *
         * @return bit flag; {@link #NO_CHECK} returns {@code null}
         */
        public Integer getValue() {
            return value;
        }
    }

    /**
     * The original client-provided filename.
     */
    final String fileName;

    /**
     * The Content-Type declared by the client.
     */
    final String contentType;

    /**
     * The Content-Type validation policy.
     */
    final Policy policy;

    /**
     * The file detection category used for pass-list checks.
     */
    final DetectType detectType;

    /**
     * Creates a validator from an upload file and configuration.
     *
     * @param file   upload file
     * @param config upload configuration
     */
    public ContentTypePolicy(MultipartFile file, FileUploadConfig config) {
        this.fileName = file.getOriginalFilename();
        this.contentType = file.getContentType();
        this.policy = config.getContentTypePolicy();
        this.detectType = config.getDetectType();
    }

    /**
     * Executes the validations included in the current policy.
     *
     * @throws IllegalArgumentException thrown when Content-Type does not match the category or mapping rules
     * @throws UncheckedIOException     thrown when probing the extension mapping fails
     */
    public void check() {
        if (policy == Policy.NO_CHECK)
            return;

        Integer value = policy.getValue();

        if ((value & Policy.WHITELIST.value) == Policy.WHITELIST.value)
            simpleCheck();

        if ((value & Policy.MAPPING.value) == Policy.MAPPING.value)
            checkMapping();
    }

    /**
     * Pass-list check of content-type.
     */
    void simpleCheck() {
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
    void checkMapping() {
        Path fakePath = Paths.get(fileName);

        try {
            String expectedByExt = Files.probeContentType(fakePath);

            if (expectedByExt == null)
                throw new IllegalArgumentException("Cannot determine Content-Type from file extension: " + fileName);

            if (contentType == null)
                throw new IllegalArgumentException("The uploaded file has no Content-Type.");

            MediaType expected = MediaType.parseMediaType(expectedByExt);
            MediaType actual = MediaType.parseMediaType(contentType);

            if (!expected.getType().equalsIgnoreCase(actual.getType())
                    || !expected.getSubtype().equalsIgnoreCase(actual.getSubtype()))
                throw new IllegalArgumentException("The uploaded Content-Type does not match the file extension.");
        } catch (IOException e) {
            throw new UncheckedIOException("checkMapping", e);
        }
    }

    /**
     * Allowed image Content-Type values.
     */
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

    /**
     * Allowed office file Content-Type values.
     */
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
            "application/vnd.oasis.opendocument.presentation", // .odp
            "text/markdown", // md
            "text/plain", // txt
            "application/wps-office.wps", // .wps
            "application/x-wps-office-document",
            "application/wps-office.et",
            "application/wps-office.dps"
    );

    /**
     * Allowed audio Content-Type values.
     */
    static final Set<String> AUDIO_CONTENT_TYPES = ObjectHelper.setOf(
            "audio/mpeg",           // .mp3
            "audio/x-wav",          // .wav
            "audio/wav",            // .wav (alternative)
            "audio/wave",           // .wav
            "audio/vnd.wave",
            "audio/x-pn-wav",       // .wav (streaming)
            "audio/aac",            // .aac
            "audio/mp4",            // .m4a, .mp4 (audio)
            "audio/x-m4a",          // .m4a
            "audio/ogg",            // .ogg, .oga
            "audio/webm",           // .webm (audio)
            "audio/3gpp",           // .3gp (audio only)
            "audio/3gpp2",          // .3g2 (audio only)
            "audio/amr",            // .amr
            "audio/flac",           // .flac
            "audio/x-caf"           // apple audio
    );

    /**
     * Allowed video Content-Type values.
     */
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
