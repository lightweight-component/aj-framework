package com.ajaxjs.dataservice.datasource;

import com.ajaxjs.dataservice.metadata.ColumnQuery;
import com.ajaxjs.dataservice.metadata.TableQuery;
import com.ajaxjs.dataservice.metadata.model.Column;
import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.JdbcConnection;
import com.ajaxjs.sqlman.crud.page.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据源配置和数据库元数据查询的服务实现。
 */
@Slf4j
@Service
public class DatasourceService implements DatasourceController {
    /**
     * 表元数据分页的默认页大小。
     */
    private static final int DEFAULT_TABLE_PAGE_SIZE = 99;

    /**
     * 表元数据分页允许的最大页大小，避免一次读取过多表注释。
     */
    private static final int MAX_TABLE_PAGE_SIZE = 500;

    @Override
    public List<DataSourceInfo> list() {
        List<DataSourceInfo> list = new Action("SELECT * FROM ds_datasource WHERE stat != 1").query().list(DataSourceInfo.class);
        if (list == null)
            return Collections.emptyList();

        list.forEach(item -> item.setPassword(null));

        return list;
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
        checkIfIsRepeat(entity, null);

        return new Action(entity).create().execute(true, Long.class).getNewlyId();
    }

    /**
     * 检查数据源编码是否与其他有效数据源重复。
     *
     * @param entity 待检查的数据源配置
     * @param dsId   数据源 id；非 {@code null} 时更新操作会排除自身
     * @throws IllegalArgumentException 当存在相同编码的数据源时抛出
     */
    void checkIfIsRepeat(DataSourceInfo entity, Long dsId) {
        if (entity == null || !StringUtils.hasText(entity.getUrlDir()))
            throw new IllegalArgumentException("缺少数据源编码 urlDir");

        String sql = dsId == null ? "SELECT id FROM ds_datasource WHERE url_dir = ? LIMIT 1"
                : "SELECT id FROM ds_datasource WHERE url_dir = ? AND id != ? LIMIT 1";

        Long id = dsId == null ? new Action(sql).query(entity.getUrlDir()).one(Long.class)
                : new Action(sql).query(entity.getUrlDir(), dsId).one(Long.class);

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
        if (id == null)
            throw new IllegalArgumentException("缺少 id 参数");

        String sql = "UPDATE ds_datasource SET stat = 1 WHERE id = ? AND stat != 1";

        return new Action(sql).update(id).execute().getEffectedRows() > 0;
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

    /**
     * 根据数据源配置标识创建 JDBC 连接。
     *
     * @param id 数据源配置主键
     * @return 已建立的 JDBC 连接
     */
    Connection getConnectionByDataSourceId(Long id) {
        if (id == null)
            throw new IllegalArgumentException("缺少数据源 id 参数");

        DataSourceInfo info = new Action("SELECT * FROM ds_datasource WHERE stat!= 1 AND id =?").query(id).one(DataSourceInfo.class);

        if (info == null)
            throw new IllegalArgumentException("数据源不存在或已删除: " + id);

        return JdbcConnection.getConnection(info.getUrl(), info.getUsername(), info.getPassword());
    }

    @Override
    public PageResult<Map<String, Object>> getTableAndComment(Long dataSourceId, Integer start, Integer limit, String tableName, String dbName) {
        int pageStart = start == null ? 0 : start;
        int pageLimit = limit == null ? DEFAULT_TABLE_PAGE_SIZE : limit;

        if (pageStart < 0)
            throw new IllegalArgumentException("start 不能小于 0");

        if (pageLimit < 1 || pageLimit > MAX_TABLE_PAGE_SIZE)
            throw new IllegalArgumentException("limit 必须在 1 到 " + MAX_TABLE_PAGE_SIZE + " 之间");

        try (Connection conn = getConnectionByDataSourceId(dataSourceId)) {
            return getTableAndComment(conn, pageStart, pageLimit, tableName, dbName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回数据源下的表名和表注释，支持分页和表名搜索。
     *
     * @param connection 已建立的数据库连接
     * @param start      起始偏移量
     * @param limit      最多返回的记录数
     * @param tableName  表名搜索关键字
     * @param dbName     数据库名称
     * @return 表名及其注释的分页结果
     * @throws SQLException 当读取数据库元数据失败时抛出
     */
    static PageResult<Map<String, Object>> getTableAndComment(Connection connection, Integer start, Integer limit, String tableName, String dbName) throws SQLException {
        int total;
        List<Map<String, Object>> list;

        try {
            TableQuery tableQuery = new TableQuery(connection);
            List<String> allTableName = tableQuery.getAllTableName(dbName);
            allTableName.remove("adp_data_service"); // 有可能出现配置表本身，删除

            if (StringUtils.hasText(tableName)) // 搜索关键字
                allTableName = allTableName.stream().filter(item -> item.contains(tableName)).collect(Collectors.toList());

            Collections.sort(allTableName);
            total = allTableName.size();

            int end = (int) Math.min((long) start + limit, total);
            List<String> subList = start >= total ? Collections.emptyList() : new ArrayList<>(allTableName.subList(start, end));
            list = subList.isEmpty() ? Collections.emptyList() : tableQuery.getTableCommentWithAnnotateAsList(subList, dbName);
        } finally {
            JdbcConnection.closeDb(connection);
        }

        PageResult<Map<String, Object>> result = new PageResult<>();
        result.setList(list);
        result.setTotalCount(total);

        return result;
    }

    /**
     * 根据创建表的 SQL 语句获取注释
     *
     * @param createTableSql 创建表的 SQL 语句
     * @return 注释内容，如果不存在注释则返回 null
     */
    static String getCommentFromCreateTableSql(String createTableSql) {
        int commentStartIndex = createTableSql.indexOf("COMMENT='");// 查找注释起始位置

        if (commentStartIndex == -1)
            return null;

        int commentEndIndex = createTableSql.indexOf("'", commentStartIndex + 9);// 查找注释结束位置

        if (commentEndIndex == -1)
            return null;

        return createTableSql.substring(commentStartIndex + 9, commentEndIndex);
    }
}
