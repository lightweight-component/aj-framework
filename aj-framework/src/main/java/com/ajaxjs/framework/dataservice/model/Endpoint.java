package com.ajaxjs.framework.dataservice.model;

import com.ajaxjs.framework.dataservice.constant.ActionType;
import com.ajaxjs.util.httpremote.HttpConstant;
import lombok.Data;

@Data
public class Endpoint {
    Integer id;

    /**
     * Equals to parent id.
     */
    Integer groupId;

    HttpConstant.HttpMethod method;


    String url;

    /**
     * Equals to a key to locate this endpoint.
     */
    String urlMethod;

    String sql;

    String name;

    ActionType actionType;

    /**
     * If it's true, you need to specify the tableName.
     */
    boolean isAutoSql;

    String tableName;

}
