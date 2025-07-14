package com.ajaxjs.devtools.mysqltools.metadata.model;


import lombok.Data;

import javax.sql.DataSource;
import java.util.Date;

/**
 * 数据库的数据源
 *
 * @author Frank Cheung sp42@qq.com
 */

@Data
public class DataSourceInfo {
    /**
     * 主键
     */
    private Long id;

    /**
     * 唯一 id
     */
    private Long uid;

    /**
     * 数据字典：状态
     */
    private Integer stat;

//	private Status stat;

    private String name;

    private String content;

    /**
     * 创建日期
     */
    private Date createDate;

    /**
     * 修改日期
     */
    private Date updateDate;

    /**
     * 数据库厂商
     */
    public static enum DatabaseType {
        MY_SQL, SQL_SERVER, ORACLE, POSTGRES_SQL, DB2, SQLITE, SPARK
    }

    /**
     * 数据库厂商：my_sql， sql_server， oracle， postgres_sql， db2， sqlite， spark
     */
    private DatabaseType type;

    /**
     * 连接地址
     */
    private String url;

    /**
     * 数据源编码，唯一
     */
    private String urlDir;

    /**
     * 数据库用户账号
     */
    private String username;

    /**
     * 数据库账号密码
     */
    private String password;

    /**
     * 是否跨库
     */
    private Boolean crossDb;

    /**
     * 数据源实例
     */
    private DataSource instance;
}
