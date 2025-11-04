package com.ajaxjs.framework.dataservice.model;

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

    PAGE_LIST,

    CREATE,

    UPDATE,

    DELETE
}
