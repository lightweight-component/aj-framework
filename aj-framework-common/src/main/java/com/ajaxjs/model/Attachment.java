package com.ajaxjs.model;

import com.ajaxjs.framework.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 附件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Attachment extends BaseModel {
    /**
     * 附件名称
     */
    private String fileName;

    /**
     * 附件路径-绝对路径，上传到磁盘才有，网络存储的没有
     */
    private String filePath;

    /**
     * 归属的业务表
     */
    private String tableName;

    /**
     * 归属的业务名称
     */
    private String businessName;

    /**
     * 归属的业务表对应记录主键
     */
    private Serializable pkId;

    /**
     * 附件类型-自定义，如CERT-1:证件正面 CERT-2:证件反面
     */
    private String type;
}
