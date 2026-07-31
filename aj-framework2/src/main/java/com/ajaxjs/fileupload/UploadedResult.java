package com.ajaxjs.fileupload;

import lombok.Data;

/**
 * 文件上传结果，可作为控制器返回值序列化给客户端。
 */
@Data
public class UploadedResult {
    /**
     * 文件访问 URL。
     */
    String url;

    /**
     * 实际保存的文件名；根据命名策略可能不同于原始文件名。
     */
    String fileName;

    /**
     * 客户端提供的原始文件名。
     */
    String originalFileName;

    /**
     * 文件大小，单位字节。
     */
    long fileSize;
}
