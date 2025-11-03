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

    /**
     * Required when Map data is used and custom SQL is not used, to specify the table name.
     */
    String tableName;

    /**
     * Required when doing the creation of an entity, to know if it's auto increment ID.
     */
    boolean isAutoIns;

    /**
     * Required when doing the update of an entity, to know which field is the ID.
     */
    String idField;

}
