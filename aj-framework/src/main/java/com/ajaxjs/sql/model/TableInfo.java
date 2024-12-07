package com.ajaxjs.sql.model;

import com.ajaxjs.sql.JdbcCRUD;
import lombok.Data;

@Data
public class TableInfo {
    private String tableName;

    private JdbcCRUD crud;

    private IdField idField;
}
