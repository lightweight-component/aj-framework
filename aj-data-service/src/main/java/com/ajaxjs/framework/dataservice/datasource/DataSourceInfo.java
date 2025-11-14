package com.ajaxjs.framework.dataservice.datasource;

import com.ajaxjs.sqlman.model.DatabaseVendor;
import lombok.Data;

import javax.sql.DataSource;
import java.util.Date;

/**
 * 数据库的数据源
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
    private DatabaseVendor type;

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
