package com.ajaxjs.dataservice.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 抽象基类，用于数据库元数据查询
 */
public abstract class BaseMetaQuery {
    static final Pattern MYSQL_IDENTIFIER = Pattern.compile("[A-Za-z0-9_$]+");
    /**
     * 执行元数据查询所使用的 JDBC 连接。
     */
    Connection conn;

    /**
     * 使用指定 JDBC 连接创建元数据查询器。
     *
     * @param conn 用于执行查询的 JDBC 连接
     */
    public BaseMetaQuery(Connection conn) {
        assertMySqlCompatible(conn);
        this.conn = conn;
    }

    /**
     * 确认连接的数据库产品支持本包使用的 MySQL 元数据语法。
     *
     * @param conn 待检查的 JDBC 连接
     * @throws IllegalArgumentException 当连接为空或数据库不是 MySQL/MariaDB 时抛出
     * @throws IllegalStateException 当无法读取数据库产品信息时抛出
     */
    static void assertMySqlCompatible(Connection conn) {
        if (conn == null)
            throw new IllegalArgumentException("数据库连接不能为空");

        try {
            DatabaseMetaData metaData = conn.getMetaData();
            String productName = metaData == null ? null : metaData.getDatabaseProductName();

            if (productName == null || !("MySQL".equalsIgnoreCase(productName) || "MariaDB".equalsIgnoreCase(productName)))
                throw new IllegalArgumentException("metadata 包仅支持 MySQL/MariaDB，当前数据库为: " + productName);
        } catch (SQLException e) {
            throw new IllegalStateException("无法读取数据库产品信息", e);
        }
    }

    static String quoteIdentifier(String identifier) {
        if (identifier == null || !MYSQL_IDENTIFIER.matcher(identifier).matches())
            throw new IllegalArgumentException("非法 MySQL 标识符: " + identifier);

        return "`" + identifier + "`";
    }

    static String quoteTable(String database, String table) {
        String quotedTable = quoteIdentifier(table);
        return database == null || database.isEmpty() ? quotedTable : quoteIdentifier(database) + "." + quotedTable;
    }

    /**
     * 获取查询结果，返回 List 类型的数据集合
     *
     * @param <T> 范型，要返回的 List 中元素的类别
     * @param sql 要执行的 SQL 语句
     * @param cb  对于每个查询结果行，执行回调函数，将 ResultSet 转成 Java 对象 T
     * @param clz Java 对象的类别
     * @return 返回转换后的 Java 对象 T 所组成的 List
     */
    public <T> List<T> getResult(String sql, Function<ResultSet, T> cb, Class<T> clz) {
        List<T> list = new ArrayList<>();

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                T v = cb.apply(rs);
                list.add(v);
            }
        } catch (SQLException e) {
            throw new MetadataQueryException("执行元数据列表查询失败: " + sql, e);
        }

        return list;
    }

    /**
     * 获取查询结果，返回 Map 类型的数据集合
     *
     * @param sql    要执行的 SQL 语句
     * @param cb     对于每个查询结果行执行的回调，将 ResultSet 转成 {@code Map<String, String>}
     *               <p>
     *               注意：Map 的 key 是列名（column name），value 是列值（column value）
     * @param isLoop 是否需要循环处理 ResultSet 中的每一行
     * @return 由转换结果组成的映射对象，可能包含多行数据
     */
    public Map<String, String> getMapResult(String sql, BiConsumer<ResultSet, Map<String, String>> cb, boolean isLoop) {
        Map<String, String> map = new HashMap<>();

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (isLoop) {
                while (rs.next())
                    cb.accept(rs, map);
            } else
                cb.accept(rs, map);
        } catch (SQLException e) {
            throw new MetadataQueryException("执行元数据映射查询失败: " + sql, e);
        }

        return map;
    }

    /**
     * 获取查询结果，返回 Map 类型的数据集合。
     * <p>
     * 默认为需要循环处理 ResultSet 中的每一行
     *
     * @param sql 要执行的 SQL 语句
     * @param cb  对于每个查询结果行执行的回调，将 ResultSet 转成 {@code Map<String, String>}
     *            <p>
     *            注意：Map 的 key 是列名（column name），value 是列值（column value）
     * @return 由转换结果组成的映射对象，可能包含多行数据
     */
    public Map<String, String> getMapResult(String sql, BiConsumer<ResultSet, Map<String, String>> cb) {
        return getMapResult(sql, cb, true);
    }
}
