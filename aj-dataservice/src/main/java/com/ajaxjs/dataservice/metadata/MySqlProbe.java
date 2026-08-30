package com.ajaxjs.dataservice.metadata;

import com.ajaxjs.dataservice.metadata.model.*;
import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于读取 MySQL 数据库、表和运行环境元数据的工具。
 */
@Slf4j
@RestController
@RequestMapping("/db_meta")
public class MySqlProbe {
    @Autowired
    DataSource ds;

    @GetMapping("/test")
    DataBaseDetail test() {
        try (Connection connection = ds.getConnection()) {
            return detail(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/table_list")
    List<TableDesc> tableList() {
        try (Connection connection = ds.getConnection()) {
            return list(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/table_info/{tableName}")
    TableDetailRes tableInfo(@PathVariable String tableName) {
        try (Connection connection = ds.getConnection()) {
            return detail(connection, getDatabaseNameByConnection(connection), tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取当前连接数据库的详情。
     *
     * @param conn 已连接的 JDBC 连接
     * @return 当前数据库的详情
     */
    public static DataBaseDetail detail(Connection conn) {
        return detail(conn, getDatabaseNameByConnection(conn));
    }

    /**
     * 从 JDBC 连接中读取当前数据库名称。
     *
     * @param conn 已连接的 JDBC 连接
     * @return 当前连接的 catalog，即数据库名称
     * @throws RuntimeException 当读取 JDBC 元数据失败时抛出
     */
    static String getDatabaseNameByConnection(Connection conn) {
        String catalog; // catalog 是数据库名

        try {
            catalog = conn.getCatalog();
        } catch (SQLException e) {
            log.warn("Error when MySqlProbe", e);
            throw new RuntimeException(e);
        }

        log.info("---------连接成功，数据库：" + catalog);

        return catalog;
    }

    /**
     * 获取指定 MySQL 数据库的运行状态和配置信息。
     *
     * @param conn     已连接的 JDBC 连接
     * @param database 要统计的数据库名称
     * @return 数据库详情
     * @throws RuntimeException 当读取数据库元数据失败时抛出
     */
    public static DataBaseDetail detail(Connection conn, String database) {
        MetaQuery q = new MetaQuery(conn);

        Map<String, String> maxConnection = q.getVariables(" SHOW STATUS LIKE 'connections'; ");
        maxConnection.putAll(q.getVariables(" SHOW VARIABLES LIKE '%max_connections%' "));

        Map<String, String> threadsCached = q.getVariables(" SHOW STATUS LIKE 'threads_cached' ");
        Map<String, String> threadsConnected = q.getVariables(" SHOW STATUS LIKE 'threads_connected' ");
        Map<String, String> threadsCreated = q.getVariables(" SHOW STATUS LIKE 'threads_created' ");
        Map<String, String> threadsRunning = q.getVariables(" SHOW STATUS LIKE 'threads_running' ");
        Map<String, String> slowLaunchThreads = q.getVariables(" SHOW STATUS LIKE 'slow_launch_threads' ");
        threadsCached.putAll(threadsConnected);
        threadsCached.putAll(threadsCreated);
        threadsCached.putAll(threadsRunning);
        threadsCached.putAll(slowLaunchThreads);

        Map<String, String> basicInfo;

        try {
            DatabaseMetaData metaData = conn.getMetaData();
            String url = metaData.getURL();

            // 使用正则表达式提取 IP 地址和端口号
            Matcher matcher = Pattern.compile("//(.*):(\\d+)/").matcher(url);
            String ip = "", port = "";

            if (matcher.find()) {
                ip = matcher.group(1);
                port = matcher.group(2);
            }

            basicInfo = ObjectHelper.mapOf("name", metaData.getDriverName(), "ip", ip, "database", conn.getCatalog());
            basicInfo.put("port", port);
            basicInfo.put("userName", metaData.getUserName());
        } catch (Exception e) {
            log.warn("Error when MySqlProbe", e);
            throw new RuntimeException(e);
        }

        DataBaseDetail detail = new DataBaseDetail();
        detail.setBasicInfo(basicInfo);
        detail.setMySqlHome(getCustomProperties("MYSQL_HOME"));
        detail.setBasedir(q.getVariable("Value", "SHOW VARIABLES LIKE '%basedir%'"));
        detail.setVariables(q.getAllVariable());
        detail.setVersion(q.getVariable("version", "SELECT VERSION() AS version"));
        detail.setCharMap(q.getVariables("SHOW VARIABLES LIKE \"char%\""));
        detail.setLogError(q.getVariables("SHOW VARIABLES LIKE 'log_error'"));
        detail.setLogBin(q.getVariables("SHOW VARIABLES LIKE 'log_bin'"));
        detail.setGeneralLog(q.getVariables("SHOW VARIABLES LIKE '%general%';"));
        detail.setSlowQueryLog(q.getVariables(" SHOW VARIABLES LIKE 'slow_query%'"));
        detail.setMaxConnection(maxConnection);
        detail.setThreads(threadsCached);
        detail.setTableLock(q.getVariables(" SHOW STATUS LIKE 'table%' "));
        detail.setDataDir(q.getVariable("Value", "SHOW VARIABLES LIKE '%datadir%'"));
        detail.setDbSize(q.getDbSize(database));

        return detail;
    }

    /**
     * 获取当前连接数据库下的所有表信息。
     *
     * @param conn 已连接的 JDBC 连接
     * @return 表信息列表
     */
    public static List<TableDesc> list(Connection conn) {
        return list(conn, getDatabaseNameByConnection(conn));
    }

    /**
     * 获取指定数据库下的所有表信息。
     *
     * @param conn     已连接的 JDBC 连接
     * @param database 数据库名称
     * @return 表信息列表
     */
    public static List<TableDesc> list(Connection conn, String database) {
        MetaQuery q = new MetaQuery(conn);
        List<String> tables = q.getTables("SHOW TABLES IN " + database);
        Map<String, TableDesc> map = q.getTableDesc(database, tables);
        List<TableDesc> tableDescMain = new ArrayList<>(map.size());

        for (String key : map.keySet())
            tableDescMain.add(map.get(key));

        return tableDescMain;
    }

    /**
     * 获取指定表的建表语句、列和索引信息。
     *
     * @param connect   已连接的 JDBC 连接
     * @param database  数据库名称
     * @param tableName 表名称
     * @return 表详情
     */
    public static TableDetailRes detail(Connection connect, String database, String tableName) {
        MetaQuery q = new MetaQuery(connect);
        Map<String, String> createTable = q.getVariables("SHOW CREATE TABLE " + database + "." + tableName);
        List<TableColumns> tableColumns = q.getTableColumns(database, tableName);
        List<TableIndex> tableIndex = q.getTableIndex("SHOW INDEX FROM " + database + "." + tableName);

        TableDetailRes tableDetailRes = new TableDetailRes();
        tableDetailRes.setCreateTable(createTable);
        tableDetailRes.setTableColumns(tableColumns);
        tableDetailRes.setTableIndex(tableIndex);

        return tableDetailRes;
    }

    /**
     * 使用 System 获取系统相关的值
     */
    public static void getSystemProperties() {
        Properties pp = System.getProperties();
        Enumeration<?> en = pp.propertyNames();

        while (en.hasMoreElements()) {
            String nextE = (String) en.nextElement();
            System.out.print(nextE + "=" + pp.getProperty(nextE));
        }
    }

    /**
     * 从当前进程环境变量中读取指定属性。
     *
     * @param key 环境变量名称
     * @return 对应的变量值；不存在时返回 {@code null}
     */
    public static String getCustomProperties(String key) {
        Map<String, String> map = getEnv();

        return map.get(key);
    }

    /**
     * 读取当前操作系统进程可见的环境变量。
     *
     * @return 环境变量名称与值的映射
     */
    public static Map<String, String> getEnv() {
        return new HashMap<>(System.getenv());
    }

    /**
     * 通过 {@code mysqladmin} 检查 MySQL 服务是否响应。
     * 只能在 Linux 下执行。
     *
     * @param username MySQL 用户名
     * @param password MySQL 密码
     * @return 命令输出或错误提示
     * @author <a href="https://github.com/535404515/MYSQL-TOMCAT-MONITOR/blob/master/nlpms-task-monitor/src/main/java/com/nuoli/mysqlprotect/timer/MysqlServiceJob.java">...</a>
     */
    public static String ping(String username, String password) {
        Process p;

        try {
            p = new ProcessBuilder("mysqladmin", "-u" + username, "-p" + password, "ping").start();
        } catch (IOException e) {
            return "获取 mysql 是否停止异常";
        }

        byte[] b = new byte[1024];
        int readBytes;
        StringBuilder sb = new StringBuilder();

        try (InputStream in = p.getInputStream()) {
            while ((readBytes = in.read(b)) != -1)
                sb.append(new String(b, 0, readBytes));
        } catch (IOException e) {
            log.warn("", e);
            return "读取流异常";
        }

        return sb.toString();
    }
}
