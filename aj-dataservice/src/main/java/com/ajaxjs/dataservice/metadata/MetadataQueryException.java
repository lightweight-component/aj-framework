package com.ajaxjs.dataservice.metadata;

/**
 * 数据库元数据读取或解析失败时抛出的异常。
 */
public class MetadataQueryException extends RuntimeException {
    /**
     * 使用错误描述和根因创建异常。
     *
     * @param message 错误描述
     * @param cause   根因
     */
    public MetadataQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
