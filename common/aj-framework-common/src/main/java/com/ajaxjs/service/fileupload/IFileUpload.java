package com.ajaxjs.service.fileupload;

import com.ajaxjs.model.Attachment;
import com.ajaxjs.service.IService;

import java.io.Serializable;

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

    /**
     * 附件更新
     *
     * @param id
     * @param attachment
     * @return
     */
    boolean update(Serializable id, Attachment attachment);

    /**
     * 附件查询
     *
     * @param id
     * @return
     */
    Attachment info(Serializable id);
}
