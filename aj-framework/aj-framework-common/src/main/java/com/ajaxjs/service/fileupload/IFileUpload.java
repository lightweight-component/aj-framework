package com.ajaxjs.service.fileupload;

import com.ajaxjs.service.IService;

/**
 * 云空间的文件上传
 */
public interface IFileUpload extends IService {
    /**
     * 上传文件
     *
     * @param filename 上传文件名
     * @param bytes    文件内容
     * @return 是否成功
     */
    boolean upload(String filename, byte[] bytes);
}
