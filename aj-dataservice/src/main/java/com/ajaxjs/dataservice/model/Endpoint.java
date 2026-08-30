package com.ajaxjs.dataservice.model;

import com.ajaxjs.util.httpremote.HttpConstant;
import lombok.Data;

/**
 * 动态数据服务中一个可被 HTTP 请求调用的端点定义。
 */
@Data
public class Endpoint {
    /**
     * 端点的唯一标识。
     */
    Integer id;

    /**
     * Equals to parent id.
     */
    Integer groupId;

    /**
     * 允许访问端点的 HTTP 方法。
     */
    HttpConstant.HttpMethod method;

    /**
     * 相对于所属分组的访问路径。
     */
    String url;

    /**
     * Equals to a key to locate this endpoint.
     */
    String urlMethod;

    /**
     * 自定义查询或写入 SQL。
     */
    String sql;

    /**
     * 用于展示的端点名称。
     */
    String name;

    /**
     * 端点执行的操作类型。
     */
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
