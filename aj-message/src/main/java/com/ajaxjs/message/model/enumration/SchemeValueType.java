package com.ajaxjs.message.model.enumration;

/**
 * 方案值类型
 */
public enum SchemeValueType {
    AUTO,
    INTEGER,
    DECIMAL,
    STRING,
    BOOLEAN,
    TEXTAREA,

    // ===========以下为特殊处理的类型============
    /**
     * 接收人分组
     */
    RECEIVER_GROUP,
    /**
     * 接收人
     */
    RECEIVER,
    /**
     * 多对象输入
     */
    MULTI_OBJ_INPUT,
    /**
     * 多选
     */
    SELECT
    ;
}
