package com.ajaxjs.devtools.sysmonitor.model;

import lombok.Data;

/**
 * 操作系统信息
 */
@Data
public class SysInfo {

    /**
     * 系统名称
     */
    private String name;

    /**
     * 系统 ip
     */
    private String ip;

    /**
     * 操作系统
     */
    private String osName;

    /**
     * 系统架构
     */
    private String osArch;

    /**
     * 项目路径
     */
    private String userDir;

}
