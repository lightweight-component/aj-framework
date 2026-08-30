package com.ajaxjs.dataservice.metadata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表信息查询
 */
@Slf4j
public class TableQuery extends BaseMetaQuery {
    /**
     * MySQL 建表语句中表或字段注释选项的起始标记。
     */
    private static final Pattern COMMENT_START = Pattern.compile("(?i)\\bCOMMENT\\s*=\\s*'");

    /**
     * 使用指定 JDBC 连接创建表元数据查询器。
     *
     * @param conn 用于执行查询的 JDBC 连接
     */
    public TableQuery(Connection conn) {
        super(conn);
    }

    /**
     * 获取当前数据库下的所有表名称
     *
     * @param dbName 数据库名，可选的
     * @return 所有表名称
     */
    public List<String> getAllTableName(String dbName) {
        String sql = StringUtils.hasText(dbName) ? "SHOW TABLES FROM " + quoteIdentifier(dbName) : "SHOW TABLES";

        return getResult(sql, rs -> {
            try {
                return rs.getString(1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, String.class);
    }

    /**
     * 获得某表的注释 注意这个方法并不会关闭数据库连接
     *
     * @param tableName 表名
     * @return 表注释
     */
    public String getTableComment(String tableName) {
        return getMapResult("SHOW CREATE TABLE " + quoteIdentifier(tableName), (rs, map) -> {
            try {
                String createDDL = rs.getString(2);
                map.put("comment", parse(createDDL));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).get("comment");
    }

    /**
     * 返回注释信息
     *
     * @param all DDL
     * @return 注释信息
     */
    public static String parse(String all) {
        if (all == null)
            return null;

        Matcher matcher = COMMENT_START.matcher(all);
        String comment = "";

        while (matcher.find()) {
            StringBuilder value = new StringBuilder();
            boolean closed = false;

            for (int i = matcher.end(); i < all.length(); i++) {
                char current = all.charAt(i);

                if (current == '\\' && i + 1 < all.length()) {
                    value.append(all.charAt(++i));
                } else if (current == '\'') {
                    if (i + 1 < all.length() && all.charAt(i + 1) == '\'') {
                        value.append(current);
                        i++;
                    } else {
                        closed = true;
                        break;
                    }
                } else
                    value.append(current);
            }

            if (closed)
                comment = value.toString();
        }

        return comment;
    }

    /**
     * 获得多张表的注释 注意这个方法并不会关闭数据库连接。
     *
     * @param tableNames 表名集合
     * @param dbName     数据库名，可选的
     * @return 表注释集合，key 是表名，value 是注释
     */
    public Map<String, String> getTableComment(List<String> tableNames, String dbName) {
        Map<String, String> map = new HashMap<>();
        boolean hasDbName = StringUtils.hasText(dbName);

        try (Statement stmt = conn.createStatement()) {
            for (String tableName : tableNames) {
                String t = quoteTable(hasDbName ? dbName : null, tableName);

                try (ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + t)) {
                    String createDDL = null;

                    try {
                        if (rs.next())
                            createDDL = rs.getString(2);
                    } catch (SQLException e) {
                        throw new MetadataQueryException("读取表建表语句失败: " + tableName, e);
                    }

                    String comment = TableQuery.parse(createDDL);
                    map.put(tableName, comment);
                }
            }
        } catch (SQLException e) {
            throw new MetadataQueryException("读取表注释失败", e);
        }

        return map;
    }

    /**
     * 获得多张表的注释，返回的 Map 带有 key 注解的，并保存到 List 中
     * 注意这个方法并不会关闭数据库连接。
     *
     * @param tableNames 表名集合
     * @param dbName     数据库名，可选的
     * @return 表注释集合，固定 key，分别是 tableName、comment
     */
    public List<Map<String, Object>> getTableCommentWithAnnotateAsList(List<String> tableNames, String dbName) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, String> tableComment = getTableComment(tableNames, dbName);

        for (String tableName : tableComment.keySet().stream().sorted().collect(java.util.stream.Collectors.toList())) {
            Map<String, Object> map = new HashMap<>();
            map.put("tableName", tableName);
            map.put("comment", tableComment.get(tableName));

            list.add(map);
        }

        return list;
    }
}
