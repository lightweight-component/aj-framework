package com.ajaxjs.fileupload;

/**
 * 上传文件的内容检测类别。
 * <p>该类别用于选择扩展名、Content-Type 和魔数校验规则，并不表示文件已经过完整解析。</p>
 */
public enum DetectType {
    /**
     * 不限定文件类别。
     */
    NONE,

    /**
     * 图片文件。
     */
    IMAGE,

    /**
     * Office、PDF、文本等文档文件。
     */
    OFFICE_FILE,

    /**
     * 音频文件。
     */
    AUDIO,

    /**
     * 视频文件。
     */
    VIDEO
}
