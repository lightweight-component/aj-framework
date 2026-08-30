package com.ajaxjs.dataservice.model;

/**
 * 动态端点支持的执行动作。
 */
public enum ActionType {
    /**
     * Single value, a string, a number or a boolean
     */
    VALUE,

    /**
     * Single entity information
     */
    INFO,

    /**
     * List of entities
     */
    LIST,

    /**
     * 分页查询多条记录。
     */
    PAGE_LIST,

    /**
     * 创建一条记录。
     */
    CREATE,

    /**
     * 更新一条或多条记录。
     */
    UPDATE,

    /**
     * 删除一条或多条记录。
     */
    DELETE
}
