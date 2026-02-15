package com.ajaxjs.framework.dataservice.tools;

import com.ajaxjs.util.date.DateTools;
import com.ajaxjs.util.io.FileHelper;
import com.ajaxjs.util.io.ZipHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 免 mysqldump 命令备份 SQL
<pre>{@code
@Scheduled(cron = "0 0 23 * * ?")  // 每天晚上11点执行
 public void backup() {
     String path = "/home/backup/mysql";
     new File(path).mkdirs();
     try (Connection connection = DataBaseConnection.initDb()) {
         new MysqlExport(connection, path).export();
     } catch (Exception e) {
        log.error("Failed to backup database", e);
     }
 }
}</pre>
 */
@Slf4j
public class MysqlExport {
    /**
     * ResultSet 处理器
     *
     * @param stmt   Statement 对象
     * @param sql    SQL 语句
     * @param handle 控制器
     */
    public static void rsHandle(Statement stmt, String sql, Consumer<ResultSet> handle) {
        try (ResultSet rs = stmt.executeQuery(sql)) {
            handle.accept(rs);
        } catch (SQLException e) {
            log.warn("Failed to handle sql:" + sql, e);
        }
    }

    /**
     * 创建 MysqlExport 对象
     *
     * @param conn       数据库连接对象
     * @param saveFolder 保存目录
     */
    public MysqlExport(Connection conn, String saveFolder) {
        try {
            databaseName = conn.getCatalog();
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        } catch (SQLException e) {
            log.error("Failed tp get the name of database failed, or the statement failed");
            throw new RuntimeException(e);
        }

        this.saveFolder = saveFolder;
    }

    private static final String SQL_START_PATTERN = "-- start";

    private static final String SQL_END_PATTERN = "-- end";

    /**
     * 执行语句
     */
    private final Statement stmt;

    /**
     * 数据库名
     */
    private final String databaseName;

    /**
     * 导出 SQL 的目录
     */
    private final String saveFolder;

    /**
     * 获取当前数据库下的所有表名称
     *
     * @return List<String> 所有表名称
     */
    private List<String> getAllTables() {
        List<String> tables = new ArrayList<>();

        rsHandle(stmt, "SHOW TABLE STATUS FROM " + escapeIdentifier(databaseName), rs -> {
            try {
                while (rs.next()) tables.add(rs.getString("Name"));
            } catch (SQLException e) {
                log.warn("Failed to get all tables.", e);
            }
        });

        return tables;
    }

    /**
     * 生成 create 语句
     *
     * @param table 表名
     * @return String
     */
    private String getTableInsertStatement(String table) {
        StringBuilder sql = new StringBuilder();

        try (ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + escapeIdentifier(table))) {
            while (rs.next()) {
                String qtbl = rs.getString(1), query = rs.getString(2);
                query = query.trim().replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS");

                sql.append("\n\n--");
                sql.append("\n").append(SQL_START_PATTERN).append(" table dump: ").append(qtbl);
                sql.append("\n--\n\n");
                sql.append(query).append(";\n\n");
            }

            sql.append("\n\n--\n").append(SQL_END_PATTERN).append(" table dump: ").append(table).append("\n--\n\n");
        } catch (SQLException e) {
            log.warn("Failed to create the CREATE statement", e);
        }

        return sql.toString();
    }

    /**
     * 生成insert语句
     *
     * @param table the table to get an insert statement for
     * @return String generated SQL insert
     */
    private String getDataInsertStatement(String table) {
        StringBuilder sql = new StringBuilder();

        rsHandle(stmt, "SELECT * FROM " + escapeIdentifier(table), rs -> {
            try {
//                rs.last();
//				int rowCount = rs.getRow();
//			if (rowCount <= 0)
//				return sql.toString();
                sql.append("\n--").append("\n-- Inserts of ").append(table).append("\n--\n\n");
                sql.append("\n/*!40000 ALTER TABLE `").append(table).append("` DISABLE KEYS */;\n");
                sql.append("\n--\n").append(SQL_START_PATTERN).append(" table insert : ").append(table).append("\n--\n");
                sql.append("INSERT INTO `").append(table).append("`(");

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 0; i < columnCount; i++)
                    sql.append("`").append(metaData.getColumnName(i + 1)).append("`, ");

                sql.deleteCharAt(sql.length() - 1).deleteCharAt(sql.length() - 1).append(") VALUES \n");
                rs.beforeFirst();

                while (rs.next()) {
                    sql.append("(");

                    for (int i = 0; i < columnCount; i++) {
                        int columnType = metaData.getColumnType(i + 1), columnIndex = i + 1;

                        if (Objects.isNull(rs.getObject(columnIndex)))
                            sql.append(rs.getObject(columnIndex)).append(", ");
                        else if (columnType == Types.INTEGER || columnType == Types.TINYINT || columnType == Types.BIT)
                            sql.append(rs.getInt(columnIndex)).append(", ");
                        else {
                            String val = escapeString(rs.getString(columnIndex));
                            sql.append("'").append(val).append("', ");
                        }
                    }

                    sql.deleteCharAt(sql.length() - 1).deleteCharAt(sql.length() - 1);
                    sql.append(rs.isLast() ? ")" : "),\n");
                }
            } catch (SQLException e) {
                log.warn("Failed to process table: " + table, e);
            }
        });

        sql.append(";\n--\n").append(SQL_END_PATTERN).append(" table insert : ").append(table).append("\n--\n");
        // enable FK constraint
        sql.append("\n/*!40000 ALTER TABLE `").append(table).append("` ENABLE KEYS */;\n");

        return sql.toString();
    }

    /**
     * 导出所有表的结构和数据
     *
     * @return 完整的 SQL 备份内容
     */
    private String exportToSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("--\n-- Generated by AJAXJS-Data");
        sql.append("\n-- Date: ").append(DateTools.now("d-M-Y H:m:s")).append("\n--");

        // these declarations are extracted from HeidiSQL
        sql.append("\n\n/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;").append("\n/*!40101 SET NAMES utf8 */;\n/*!50503 SET NAMES utf8mb4 */;").append("\n/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;").append("\n/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;");

        for (String s : getAllTables()) {
            sql.append(getTableInsertStatement(s.trim()));
            sql.append(getDataInsertStatement(s.trim()));
        }

        try {
            stmt.close();
        } catch (SQLException e) {
            log.warn("Failed to close statement", e);
        }

        sql.append("\n/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;").append("\n/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;").append("\n/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;");

        return sql.toString();
    }

    /**
     * 执行导出
     *
     * @return 打包的文件名
     */
    public String export() {
        String fileName = "db-dump-" + DateTools.now("yyyy-MM-dd") + "-" + databaseName + ".sql";
        String sqlFile = saveFolder + File.separator + fileName;

        new FileHelper(sqlFile).writeFileContent(exportToSql());
        // 压缩 zip
        ZipHelper.zipSingleFile(sqlFile, sqlFile.replace(".sql", ".zip"), false);
        new FileHelper(sqlFile).delete();
        fileName = fileName.replace(".sql", ".zip");

        return fileName;

    }

    private String escapeIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    private String escapeString(String str) {
        if (str == null)
            return "NULL";

        // 修正后的转义逻辑
        return "'" + str.replace("\\", "\\\\") // 将 \ 替换为 \\
                .replace("'", "\\'")       // 将 ' 替换为 \'
                .replace("\n", "\\n")      // 将换行符替换为 \n
                .replace("\r", "\\r")      // 将回车符替换为 \r
                .replace("\0", "\\0")      // 将 NUL 字符替换为 \0
                + "'";
    }
}