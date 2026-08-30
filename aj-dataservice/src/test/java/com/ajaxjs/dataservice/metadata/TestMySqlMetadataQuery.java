package com.ajaxjs.dataservice.metadata;

import com.ajaxjs.dataservice.metadata.model.Column;
import com.ajaxjs.dataservice.metadata.model.Database;
import com.ajaxjs.dataservice.metadata.model.Table;
import com.ajaxjs.dataservice.metadata.model.TableColumns;
import com.ajaxjs.dataservice.metadata.model.TableIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TestMySqlMetadataQuery {
    Connection connection;

    Statement statement;

    @BeforeEach
    void setUp() throws Exception {
        connection = mock(Connection.class);
        statement = mock(Statement.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");
        when(connection.createStatement()).thenReturn(statement);
    }

    @Test
    void tableCommentUsesQualifiedDatabaseAndTableName() throws Exception {
        ResultSet resultSet = createTableResult("CREATE TABLE `orders` (id bigint) COMMENT='订单'");
        when(statement.executeQuery("SHOW CREATE TABLE `store`.`orders`")).thenReturn(resultSet);

        Map<String, String> comments = new TableQuery(connection).getTableComment(Collections.singletonList("orders"), "store");

        assertEquals("订单", comments.get("orders"));
        verify(statement).executeQuery("SHOW CREATE TABLE `store`.`orders`");
    }

    @Test
    void databaseQueryUsesQualifiedDatabaseAndTableName() throws Exception {
        ResultSet resultSet = createTableResult("CREATE TABLE `orders` (id bigint) COMMENT='订单'");
        when(statement.executeQuery("SHOW CREATE TABLE `store`.`orders`")).thenReturn(resultSet);

        List<Table> tables = new DataBaseQuery(connection).getDataBaseWithTableFull(Collections.singletonList("orders"), "store");

        assertEquals(1, tables.size());
        assertEquals("orders", tables.get(0).getName());
        assertEquals("订单", tables.get(0).getComment());
        verify(statement).executeQuery("SHOW CREATE TABLE `store`.`orders`");
    }

    @Test
    void tableDescriptionReturnsEmptyResultForDatabaseWithoutTables() {
        Map<String, ?> tables = new MetaQuery(connection).getTableDesc("empty_db", Collections.emptyList());

        assertTrue(tables.isEmpty());
        verifyNoInteractions(statement);
    }

    @Test
    void tableAndColumnQueriesMapMetadataRows() throws Exception {
        ResultSet tableResult = mock(ResultSet.class);
        when(tableResult.next()).thenReturn(true, false);
        when(tableResult.getString(1)).thenReturn("orders");
        when(statement.executeQuery("SHOW TABLES FROM `store`")).thenReturn(tableResult);
        assertEquals(Collections.singletonList("orders"), new TableQuery(connection).getAllTableName("store"));

        ResultSet columnResult = fullColumnResult();
        when(statement.executeQuery("SHOW FULL COLUMNS FROM `store`.`orders`")).thenReturn(columnResult);
        List<Column> columns = new ColumnQuery(connection).getColumnComment("orders", "store");

        assertEquals(1, columns.size());
        assertEquals("id", columns.get(0).getName());
        assertEquals(11, columns.get(0).getLength());
        assertTrue(columns.get(0).getIsKey());
    }

    @Test
    void metaQueryMapsVariablesColumnsAndIndexes() throws Exception {
        ResultSet variableResult = mock(ResultSet.class);
        when(variableResult.next()).thenReturn(true, false);
        when(variableResult.getString(1)).thenReturn("version");
        when(variableResult.getString(2)).thenReturn("8.0");
        when(statement.executeQuery("SHOW VARIABLES")).thenReturn(variableResult);
        assertEquals("8.0", new MetaQuery(connection).getAllVariable().get("version"));

        ResultSet detailResult = mock(ResultSet.class);
        when(detailResult.next()).thenReturn(true, false);
        when(detailResult.getString("Field")).thenReturn("id");
        when(detailResult.getString("Type")).thenReturn("bigint");
        when(detailResult.getString("Comment")).thenReturn("主键");
        when(statement.executeQuery("SHOW FULL COLUMNS FROM `store`.`orders`")).thenReturn(detailResult);
        List<TableColumns> columns = new MetaQuery(connection).getTableColumns("store", "orders");
        assertEquals("id", columns.get(0).getField());
        assertEquals("主键", columns.get(0).getComment());

        ResultSet indexResult = mock(ResultSet.class);
        when(indexResult.next()).thenReturn(true, false);
        when(indexResult.getString("Table")).thenReturn("orders");
        when(indexResult.getString("Key_name")).thenReturn("PRIMARY");
        when(statement.executeQuery("SHOW INDEX FROM store.orders")).thenReturn(indexResult);
        List<TableIndex> indexes = new MetaQuery(connection).getTableIndex("SHOW INDEX FROM store.orders");
        assertEquals("PRIMARY", indexes.get(0).getKeyName());
    }

    @Test
    void databaseQueryListsUserDatabaseAndParsesColumns() throws Exception {
        ResultSet databaseResult = mock(ResultSet.class);
        when(databaseResult.next()).thenReturn(true, false);
        when(databaseResult.getString("Database")).thenReturn("store");
        when(statement.executeQuery("SHOW DATABASES")).thenReturn(databaseResult);

        assertArrayEquals(new String[]{"store"}, new DataBaseQuery(connection).getDatabase());

        ResultSet tableResult = mock(ResultSet.class);
        when(tableResult.next()).thenReturn(true, false);
        when(tableResult.getString(1)).thenReturn("orders");
        when(statement.executeQuery("SHOW TABLES FROM `store`")).thenReturn(tableResult);
        Database[] databases = new DataBaseQuery(connection).getDataBaseWithTable(new String[]{"store", "mysql"});

        assertEquals(1, databases.length);
        assertEquals(Collections.singletonList("orders"), databases[0].getTables());

        List<Column> columns = new DataBaseQuery(connection).parseColumns("CREATE TABLE orders (id bigint NOT NULL, name varchar(20)) COMMENT='订单'");
        assertEquals(2, columns.size());
        assertTrue(columns.get(0).getIsRequired());
        assertEquals(20, columns.get(1).getLength());

        assertThrows(MetadataQueryException.class, () -> new DataBaseQuery(connection).parseColumns(null));
        assertThrows(MetadataQueryException.class, () -> new DataBaseQuery(connection).parseColumns("not a create table"));
    }

    ResultSet createTableResult(String ddl) throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(2)).thenReturn(ddl);

        return resultSet;
    }

    ResultSet fullColumnResult() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("Field")).thenReturn("id");
        when(resultSet.getString("Type")).thenReturn("bigint(11)");
        when(resultSet.getString("Comment")).thenReturn("主键");
        when(resultSet.getString("Default")).thenReturn(null);
        when(resultSet.getString("Key")).thenReturn("PRI");

        return resultSet;
    }
}
