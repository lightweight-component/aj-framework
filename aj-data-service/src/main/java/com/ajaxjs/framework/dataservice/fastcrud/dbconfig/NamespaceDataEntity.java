package com.ajaxjs.framework.dataservice.fastcrud.dbconfig;

import lombok.Data;

import java.util.Map;

@Data
public class NamespaceDataEntity {

    /**
     * 主键 id，自增
     */
    private Integer id;

    /**
     * 名称 KEY
     */
    private String namespace;

    /**
     * 表名
     */
    private String tableName;

    /**
     * Whether to automatically add sorting by date
     * Default: 1 (true)
     */
    private Boolean listOrderByDate;

    /**
     * Whether to add tenant data isolation
     * Default: 0 (false)
     */
    private Boolean tenantIsolation;

    /**
     * Whether to restrict the query results to include only data belonging to the current user.
     * Default: 0 (false)
     */
    private Boolean currentUserOnly;

    /**
     * Whether to filter deleted data
     * Default: 1 (true)
     */
    private Boolean filterDeleted;

    /**
     *
     */
    private Map<String, Object> tableJoin;

    /**
     * Code 8421, can be:
     * <pre>
     * 1=creator_id
     * 2=creator
     * 4=user_id
     * 8=user_name
     * </pre>
     * Common cases:
     * <pre>
     * 3=creator_id+creator
     * 7=creator_id+creator+user_id
     * 15=creator_id+creator+user_id+user_name
     * </pre>
     */
    private Integer saveUserOnCreate;

}