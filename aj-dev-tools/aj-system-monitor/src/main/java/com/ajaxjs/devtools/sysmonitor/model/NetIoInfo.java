package com.ajaxjs.devtools.sysmonitor.model;

import lombok.Data;

/**
 * 网络带宽信息
 */
@Data
public class NetIoInfo {
    /**
     * 每秒钟接收的数据包,rxpck/s
     */
    private String rxpck;

    /**
     * 每秒钟发送的数据包,txpck/s
     */
    private String txpck;

    /**
     * 每秒钟接收的KB数,rxkB/s
     */
    private String rxbyt;

    /**
     * 每秒钟发送的KB数,txkB/s
     */
    private String txbyt;


}
