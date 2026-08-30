package com.ajaxjs.dataservice.metadata;

import com.ajaxjs.dataservice.metadata.model.TableDetailRes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestMySqlProbe {
    Connection connection;

    Statement statement;

    @BeforeEach
    void setUp() throws Exception {
        connection = mock(Connection.class);
        statement = mock(Statement.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        when(connection.getCatalog()).thenReturn("store");
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");
        when(connection.createStatement()).thenReturn(statement);
    }

    @Test
    void getsCurrentDatabaseNameAndReportsSqlFailure() throws Exception {
        assertEquals("store", MySqlProbe.getDatabaseNameByConnection(connection));

        when(connection.getCatalog()).thenThrow(new SQLException("broken"));
        assertThrows(RuntimeException.class, () -> MySqlProbe.getDatabaseNameByConnection(connection));
    }

    @Test
    void readsEnvironmentVariablesFromCurrentProcess() {
        Map<String, String> environment = MySqlProbe.getEnv();

        assertFalse(environment.isEmpty());
        assertEquals(environment.get("PATH"), MySqlProbe.getCustomProperties("PATH"));
    }

    @Test
    void readsTableDetailFromRequestedDatabase() throws Exception {
        when(statement.executeQuery(anyString())).thenAnswer(invocation -> resultFor(invocation.getArgument(0)));

        TableDetailRes detail = MySqlProbe.detail(connection, "store", "orders");

        assertEquals("CREATE TABLE orders", detail.getCreateTable().get("Table"));
        assertEquals("id", detail.getTableColumns().get(0).getField());
        assertEquals("PRIMARY", detail.getTableIndex().get(0).getKeyName());
    }

    ResultSet resultFor(String sql) throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);

        if (sql.startsWith("SHOW CREATE TABLE")) {
            when(resultSet.getString(1)).thenReturn("Table");
            when(resultSet.getString(2)).thenReturn("CREATE TABLE orders");
        } else if (sql.startsWith("SHOW FULL COLUMNS")) {
            when(resultSet.getString("Field")).thenReturn("id");
            when(resultSet.getString("Type")).thenReturn("bigint");
        } else if (sql.startsWith("SHOW INDEX")) {
            when(resultSet.getString("Key_name")).thenReturn("PRIMARY");
        }

        return resultSet;
    }
}
