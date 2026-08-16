package com.ajaxjs.framework.fileupload.s3service;

/**
 * 对象存储上传的最小抽象。
 *
 * @author Frank Cheung
 */
public interface IFileUpload {
    /**
     * 上传字节内容。
     *
     * @param filename 对象名称或键
     * @param bytes    文件内容
     * @return 服务确认成功时返回 {@code true}
     */
    boolean upload(String filename, byte[] bytes);
}
