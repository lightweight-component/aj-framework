package com.ajaxjs.dataservice.tools;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestMysqlExport {
    @Test
    void writeExportStreamsTableDefinitionAndRows() throws Exception {
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(connection.getCatalog()).thenReturn("store");
        when(connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenAnswer(invocation -> resultFor(invocation.getArgument(0)));
        StringWriter output = new StringWriter();

        MysqlExport exporter = new MysqlExport(connection, "target");
        exporter.writeExport(output);

        String sql = output.toString();
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS orders"));
        assertTrue(sql.contains("INSERT INTO `orders` VALUES (1, 'O\\'Reilly');"));
        assertTrue(exporter.exportToSql().contains("INSERT INTO `orders` VALUES (1, 'O\\'Reilly');"));

        StringWriter singleTable = new StringWriter();
        exporter.getDataInsertStatement("orders", singleTable);
        assertTrue(singleTable.toString().contains("INSERT INTO `orders` VALUES (1, 'O\\'Reilly');"));
    }

    ResultSet resultFor(String sql) throws Exception {
        ResultSet result = mock(ResultSet.class);

        if (sql.startsWith("SHOW TABLE STATUS")) {
            when(result.next()).thenReturn(true, false);
            when(result.getString("Name")).thenReturn("orders");
        } else if (sql.startsWith("SHOW CREATE TABLE")) {
            when(result.next()).thenReturn(true, false);
            when(result.getString(2)).thenReturn("CREATE TABLE orders (id bigint, name varchar(20))");
        } else {
            ResultSetMetaData metaData = mock(ResultSetMetaData.class);
            when(result.next()).thenReturn(true, false);
            when(result.getMetaData()).thenReturn(metaData);
            when(metaData.getColumnCount()).thenReturn(2);
            when(result.getObject(1)).thenReturn(1);
            when(result.getObject(2)).thenReturn("O'Reilly");
            when(result.getString(2)).thenReturn("O'Reilly");
        }

        return result;
    }
}
