package com.ajaxjs.dataservice.metadata;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestDetectQuery {
    Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:metadata_parser;MODE=MySQL;DB_CLOSE_DELAY=-1");
        connection = dataSource.getConnection();

        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS metadata_ddl");
            statement.execute("CREATE TABLE metadata_ddl (ddl VARCHAR(2000) NOT NULL)");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void parseReadsTableCommentInsteadOfColumnComment() throws Exception {
        String ddl = "CREATE TABLE `orders` (`name` varchar(50) COMMENT='字段注释') ENGINE=InnoDB COMMENT = '订单表'";

        assertEquals("订单表", TableQuery.parse(readDdl(ddl)));
    }

    @Test
    void parseHandlesEscapedQuoteAndMissingComment() throws Exception {
        String escaped = "CREATE TABLE `orders` (id bigint) COMMENT='O\\'Reilly''s order'";

        assertEquals("O'Reilly's order", TableQuery.parse(readDdl(escaped)));
        assertEquals("", TableQuery.parse(readDdl("CREATE TABLE `orders` (id bigint)")));
        assertNull(TableQuery.parse(null));
    }

    @Test
    void metadataQueryRejectsH2Connection() {
        assertThrows(IllegalArgumentException.class, () -> new TableQuery(connection));
    }

    String readDdl(String ddl) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM metadata_ddl");
        }

        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO metadata_ddl (ddl) VALUES (?)")) {
            statement.setString(1, ddl);
            statement.executeUpdate();
        }

        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("SELECT ddl FROM metadata_ddl")) {
            resultSet.next();

            return resultSet.getString(1);
        }
    }
}
