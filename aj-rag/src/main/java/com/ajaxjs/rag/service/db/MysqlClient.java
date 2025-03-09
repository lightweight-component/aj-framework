package com.ajaxjs.rag.service.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlClient {
    private final Connection conn;

    public MysqlClient(String host, String user, String password, String dbName, int port) {
        this.conn = createConnection(host, user, password, dbName, port);
    }

    private Connection createConnection(String host, String user, String password, String dbName, int port) {
        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useUnicode=true&characterEncoding=UTF-8";
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Error connecting to MySQL: " + e.getMessage());
            return null;
        }
    }

    public void initUserTable() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS user (" +
                "user_id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(255) NOT NULL," +
                "password VARCHAR(255) NOT NULL" +
                ");";
        executeUpdate(createTableSql);
    }

    // 其他方法...

    private void executeUpdate(String sql) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("SQL execution error: " + e.getMessage());
        }
    }

    // 为了简洁起见，其他方法如 find_by_user_id, add_user 等将被省略
    // 但它们的实现逻辑将与Python代码类似，使用PreparedStatement来执行SQL查询和更新
}