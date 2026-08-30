package com.ajaxjs.dataservice.metadata;

import com.ajaxjs.dataservice.metadata.model.Column;
import com.ajaxjs.util.CommonConstant;
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
 * 列信息查询
 */
@Slf4j
public class ColumnQuery extends BaseMetaQuery {

    /**
     * 使用指定 JDBC 连接创建列元数据查询器。
     *
     * @param conn 用于执行查询的 JDBC 连接
     */
    public ColumnQuery(Connection conn) {
        super(conn);
    }

    /**
     * 获取一张表的各个字段的注释
     *
     * @param tableName 单张表名
     * @param dbName    数据库名，可选的
     * @return 一张表的各个字段的注释
     */
    public List<Column> getColumnComment(String tableName, String dbName) {
        String target = CommonConstant.EMPTY_STRING;

        if (StringUtils.hasText(dbName))
            target += dbName + ".";

        target += tableName;

        List<Column> list = new ArrayList<>();
//        JdbcReader.query(conn, "SHOW FULL COLUMNS FROM " + target, rs -> rs2list(rs, list));
        getMapResult("SHOW FULL COLUMNS FROM " + target, (rs, map) -> rs2list(rs, list), false);

        return list;
    }

    /**
     * 获取多张表的各个字段的注释
     *
     * @param tableNames 多张表的表名
     * @return 包含给个字段注释的 Map，key 是表名，value 是各个列。列中的Map
     */
    public Map<String, List<Column>> getColumnComment(List<String> tableNames) {
        Map<String, List<Column>> map = new HashMap<>();

        try (Statement stmt = conn.createStatement()) {
            for (String tableName : tableNames) {
                try (ResultSet rs = stmt.executeQuery("SHOW FULL COLUMNS FROM " + tableName)) {
                    List<Column> list = new ArrayList<>();
                    rs2list(rs, list);
                    map.put(tableName, list);
                }
            }
        } catch (SQLException e) {
            log.warn("getColumnComment", e);
        }

        return map;
    }

    /**
     * 提取字段类型长度的正则表达式缓存。
     */
    private static Pattern getLength;

    /**
     * 将当前结果集的列元数据行转换为列对象并加入结果列表。
     *
     * @param rs   包含列元数据的结果集
     * @param list 用于接收列对象的列表
     */
    private static void rs2list(ResultSet rs, List<Column> list) {
        if (getLength == null)
            getLength = Pattern.compile("\\((\\d+)\\)");

        try {
            while (rs.next()) {
                Column col = new Column();
                col.setName(rs.getString("Field"));
                String type = rs.getString("Type");

                Matcher m = getLength.matcher(type);
                col.setLength(m.find() ? Integer.parseInt(m.group(1)) : 0);
                col.setType(m.replaceAll(""));
                col.setComment(rs.getString("Comment"));
                col.setDefaultValue(rs.getString("Default"));

                String key = rs.getString("Key");
                col.setIsKey(StringUtils.hasText(key) && "PRI".equals(key));

                list.add(col);
            }
        } catch (SQLException e) {
            log.warn("rs2list", e);
        }
    }
}
