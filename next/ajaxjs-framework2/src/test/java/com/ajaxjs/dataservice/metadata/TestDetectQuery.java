package com.ajaxjs.dataservice.metadata;

import com.ajaxjs.dataservice.metadata.model.Column;
import com.ajaxjs.dataservice.metadata.model.Table;
import com.ajaxjs.sqlman.JdbcConnection;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestDetectQuery {
    @Test
    public void testGetDatabase() {
        Connection connection = JdbcConnection.getConnection();
        DataBaseQuery query = new DataBaseQuery(connection);
        String[] database = query.getDatabase();

        System.out.println(Arrays.toString(database));

        TableQuery tableQuery = new TableQuery(connection);
        List<String> tableNames = tableQuery.getAllTableName("aj_base");

        List<Table> dataBaseWithTableFull = query.getDataBaseWithTableFull(tableNames, null);
        System.out.println(dataBaseWithTableFull);
    }

    @Test
    public void testColumnQuery() {
        Connection connection = JdbcConnection.getConnection();
        ColumnQuery columnQuery = new ColumnQuery(connection);
        List<Column> article = columnQuery.getColumnComment("article", null);

        System.out.println(article);

        Map<String, List<Column>> list = columnQuery.getColumnComment(Collections.singletonList("article"));
        System.out.println(list);
    }

    @Test
    public void testMetaQuery() {
        Connection connection = JdbcConnection.getConnection();
        MetaQuery metaQuery = new MetaQuery(connection);

        Map<String, String> allVariable = metaQuery.getAllVariable();
        System.out.println(allVariable);

        String value = metaQuery.getVariable("Value", "SHOW VARIABLES LIKE '%basedir%'");
        System.out.println(value);
    }

    @Test
    public void testTableQuery() {
        Connection connection = JdbcConnection.getConnection();
        TableQuery tableQuery = new TableQuery(connection);
        List<String> tableName = tableQuery.getAllTableName(null);
        System.out.println(tableName);

        String adpDataService = tableQuery.getTableComment("adp_data_service");

        System.out.println(adpDataService);
    }
}
