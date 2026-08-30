package com.ajaxjs.dataservice.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestBaseMetaQuery {
    Connection connection;

    Statement statement;

    BaseMetaQuery query;

    @BeforeEach
    void setUp() throws Exception {
        connection = mock(Connection.class);
        statement = mock(Statement.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MariaDB");
        when(connection.createStatement()).thenReturn(statement);
        query = new BaseMetaQuery(connection) {
        };
    }

    @Test
    void getResultAndMapResultConvertRows() throws Exception {
        ResultSet listResult = mock(ResultSet.class);
        when(listResult.next()).thenReturn(true, true, false);
        when(listResult.getString(1)).thenReturn("first", "second");
        when(statement.executeQuery("LIST")).thenReturn(listResult);

        List<String> values = query.getResult("LIST", rs -> {
            try {
                return rs.getString(1);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }, String.class);

        assertEquals(List.of("first", "second"), values);

        ResultSet mapResult = mock(ResultSet.class);
        when(mapResult.next()).thenReturn(true, false);
        when(mapResult.getString(1)).thenReturn("version");
        when(mapResult.getString(2)).thenReturn("8.0");
        when(statement.executeQuery("MAP")).thenReturn(mapResult);

        Map<String, String> variables = query.getMapResult("MAP", (rs, map) -> {
            try {
                map.put(rs.getString(1), rs.getString(2));
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });

        assertEquals("8.0", variables.get("version"));
    }

    @Test
    void getMapResultWithoutLoopLetsCallbackControlCursor() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("name");
        when(statement.executeQuery("ONE")).thenReturn(resultSet);

        Map<String, String> map = query.getMapResult("ONE", (rs, target) -> {
            try {
                if (rs.next())
                    target.put("key", rs.getString(1));
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }, false);

        assertEquals("name", map.get("key"));
    }

    @Test
    void queryFailureThrowsMetadataException() throws Exception {
        when(statement.executeQuery("BROKEN")).thenThrow(new SQLException("broken"));

        assertThrows(MetadataQueryException.class, () -> query.getResult("BROKEN", rs -> "unused", String.class));
        assertThrows(MetadataQueryException.class, () -> query.getMapResult("BROKEN", (rs, map) -> {
        }));
    }

    @Test
    void compatibilityCheckRejectsMissingConnection() {
        assertThrows(IllegalArgumentException.class, () -> BaseMetaQuery.assertMySqlCompatible(null));
    }
}
