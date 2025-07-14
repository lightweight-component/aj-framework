package com.ajaxjs.devtools.mysqltools.metadata.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TableDetailRes {
    /**
     * 表创建信息
     */
    private Map<String, String> createTable;

    /**
     * 表列的详情信息
     */
    private List<TableColumns> tableColumns;

    /**
     * 表索引信息
     */
    private List<TableIndex> tableIndex;
}
