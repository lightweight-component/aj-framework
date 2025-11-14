package com.ajaxjs.framework.dataservice.datasource;

import com.ajaxjs.framework.dataservice.metadata.ColumnQuery;
import com.ajaxjs.framework.dataservice.metadata.TableQuery;
import com.ajaxjs.framework.dataservice.metadata.model.Column;
import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.JdbcConnection;
import com.ajaxjs.sqlman.crud.page.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DatasourceService implements DatasourceController {
    @Override
    public List<DataSourceInfo> list() {
        return new Action("SELECT * FROM ds_datasource WHERE stat != 1").query().list(DataSourceInfo.class);
    }

    @Override
    public boolean test(Long id) {
        try (Connection connection = getConnectionByDataSourceId(id)) {
            log.info(connection.getMetaData().getURL());

            return true;
        } catch (SQLException e) {
            log.warn("Test db connection err:", e);
            return false;
        }
    }

    @Override
    public long create(DataSourceInfo entity) {
        return new Action(entity).create().execute(true, Long.class).getNewlyId();
    }

    /**
     * 是否重复数据源编码
     *
     * @param dsId 数据源 id，非 null 时候表示更新排除自己
     */
    private void checkIfIsRepeat(DataSourceInfo entity, Long dsId) {
        String sql;

        if (dsId != null)
            sql = "SELECT id FROM ds_datasource WHERE url_dir = ? AND id != " + dsId + " LIMIT 1";
        else
            sql = "SELECT id FROM ds_datasource WHERE url_dir = ? LIMIT 1";

        Long id = new Action(sql).query(entity.getUrlDir()).one(Long.class);

        if (id != null)
            throw new IllegalArgumentException("已存在相同编码的数据源 " + entity.getUrlDir());
    }

    @Override
    public boolean update(DataSourceInfo entity) {
        if (entity.getId() == null)
            throw new IllegalArgumentException("缺少 id 参数");

        checkIfIsRepeat(entity, entity.getId());

        return new Action(entity).update().withId().isOk();
    }

    @Override
    public boolean delete(Long id) {
//        return CRUD.delete("adp_datasource", id);
        return true;
    }

    @Override
    public List<Map<String, Object>> getAllTablesComment(Long id) throws SQLException {
        try (Connection connection = getConnectionByDataSourceId(id)) {
            TableQuery q = new TableQuery(connection);

            return q.getTableCommentWithAnnotateAsList(q.getAllTableName(null), null);
        }
    }

    @Override
    public List<Column> getTableColumn(Long id, String tableName) throws SQLException {
        try (Connection connection = getConnectionByDataSourceId(id)) {
            return new ColumnQuery(connection).getColumnComment(tableName, null);
        }
    }

    Connection getConnectionByDataSourceId(Long id) {
        DataSourceInfo info = new Action("SELECT * FROM ds_datasource WHERE stat!= 1 AND id =?").query(id).one(DataSourceInfo.class);

        //   return new JdbcConn().getConnection(getDataSourceByDataSourceInfo(info));
        /* 貌似不能从 DataSource 获取 conn，直接创建 conn 吧 */
        return JdbcConnection.getConnection(info.getUrl(), info.getUsername(), info.getPassword());
    }

    @Override
    public PageResult<Map<String, Object>> getTableAndComment(Long dataSourceId, Integer start, Integer limit, String tableName, String dbName) {
        if (start == null)
            start = 0;

        if (limit == null)
            limit = 99;

        try (Connection conn = getConnectionByDataSourceId(dataSourceId)) {
            return getTableAndComment(conn, start, limit, tableName, dbName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回数据源下的表名和表注释，支持分页和表名搜索
     *
     * @param tablename 搜索的关键字
     * @throws SQLException
     */
    private static PageResult<Map<String, Object>> getTableAndComment(Connection _conn, Integer start, Integer limit, String tablename, String dbName) throws SQLException {
        int total;
        List<Map<String, Object>> list = null;

        try {
            TableQuery tableQuery = new TableQuery(_conn);
            List<String> allTableName = tableQuery.getAllTableName(dbName);

            // 有可能出现配置表本身，删除
            allTableName.remove("adp_data_service");

            if (StringUtils.hasText(tablename)) // 搜索关键字
                allTableName = allTableName.stream().filter(item -> item.contains(tablename)).collect(Collectors.toList());

            total = allTableName.size();

            if (total > 0) {
//                List<String> subList = allTableName.subList(start, limit); // 有坑 会返回空 List
                List<String> subList = new ArrayList<>();

                for (int i = start; i < (start + limit); i++) {
                    if (i < total)
                        subList.add(allTableName.get(i));
                }

                list = tableQuery.getTableCommentWithAnnotateAsList(subList, dbName);
            }
        } finally {
            JdbcConnection.closeDb(_conn);
        }

        PageResult<Map<String, Object>> result = new PageResult<>();

        if (list != null) {
            // 排序
            Comparator<Map<String, Object>> byName = Comparator.comparing(o -> o.get("tableName").toString());
            list.sort(byName);
            result.setList(list);
            result.setTotalCount(total);
        }

        return result;
    }

    /**
     * 根据创建表的 SQL 语句获取注释
     *
     * @param createTableSql 创建表的 SQL 语句
     * @return 注释内容，如果不存在注释则返回 null
     */
    private static String getCommentFromCreateTableSql(String createTableSql) {
        int commentStartIndex = createTableSql.indexOf("COMMENT='");// 查找注释起始位置

        if (commentStartIndex == -1)
            return null;

        int commentEndIndex = createTableSql.indexOf("'", commentStartIndex + 9);// 查找注释结束位置

        if (commentEndIndex == -1)
            return null;

        return createTableSql.substring(commentStartIndex + 9, commentEndIndex);
    }
}
