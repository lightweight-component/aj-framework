package com.ajaxjs.dataservice.tools;

import com.ajaxjs.dataservice.metadata.MetaQuery;
import com.ajaxjs.dataservice.metadata.model.*;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.io.DataReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MySqlProbe {
    /**
     * 获取数据库详情
     */
    public static DataBaseDetail detail(Connection connect, String database) {
        MetaQuery q = new MetaQuery(connect);

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
            DatabaseMetaData metaData = connect.getMetaData();
            String url = metaData.getURL();

            // 使用正则表达式提取 IP 地址和端口号
            Matcher matcher = Pattern.compile("//(.*):(\\d+)/").matcher(url);
            String ip = "", port = "";

            if (matcher.find()) {
                ip = matcher.group(1);
                port = matcher.group(2);
            }

            basicInfo = ObjectHelper.mapOf("name", metaData.getDriverName(), "ip", ip, "database", connect.getCatalog());
            basicInfo.put("port", port);
            basicInfo.put("userName", metaData.getUserName());
        } catch (Exception e) {
            log.warn("", e);
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
        detail.setLogBin(q.getVariables("SHOW VARIABLES LIKE 'log_error'"));
        detail.setGeneralLog(q.getVariables("SHOW VARIABLES LIKE '%general%';"));
        detail.setSlowQueryLog(q.getVariables(" SHOW VARIABLES LIKE 'slow_query%'"));
        detail.setMaxConnection(maxConnection);
        detail.setThreads(threadsCached);
        detail.setTableLock(q.getVariables(" SHOW STATUS LIKE 'table%' "));
        detail.setDataDir(q.getVariable("Value", "SHOW VARIABLES LIKE '%datadir%'"));
        detail.setDbSize(q.getDbSize(database));

        return detail;
    }

    public static List<TableDesc> list(Connection connect, String database) {
        MetaQuery q = new MetaQuery(connect);
        // 获取某个库下的所有表信息
        List<String> tables = q.getTables("SHOW TABLES IN " + database);
        Map<String, TableDesc> map = q.getTableDesc(database, tables);
        List<TableDesc> tableDescMain = new ArrayList<>(map.size());

        for (String key : map.keySet())
            tableDescMain.add(map.get(key));

        return tableDescMain;
    }

    /**
     * 获取某个表的详情信息
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

    public static String getCustomProperties(String key) {
        Map<String, String> map = getEnv();

        return map.get(key);
    }

    public static Map<String, String> getEnv() {
        Map<String, String> map = new HashMap<>();
        Process p;
        Runtime r = Runtime.getRuntime();
        String OS = System.getProperty("os.name").toLowerCase();

        try {
            if (OS.contains("windows 9"))
                p = r.exec("command.com /c set");
            else if ((OS.contains("nt")) || (OS.contains("windows 20")) || (OS.contains("windows xp")))
                p = r.exec("cmd.exe /c set");
            else
                p = r.exec("env"); // Unix

            new DataReader(p.getInputStream()).readAsLineString(line -> {
                String[] str = line.split("=");

                if (2 <= str.length)
                    map.put(str[0], str[1]);
            });
        } catch (IOException e) {
            log.warn("", e);
        }

        return map;
    }

    /**
     * 只能在 Linux 下执行
     *
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
