package com.ajaxjs.framework.sysmonitor;

import lombok.Data;

/**
 * Linux 网络统计信息
 * <p>
 * 数据来源：/proc/net/dev
 * 该文件包含了所有网络接口的流量统计信息，用于监控网络性能和流量分析
 * </p>
 */
@Data
public class NetworkStats {
    /**
     * 接收的总字节数（下载流量）
     * <p>
     * 数据来源：/proc/net/dev 第 1 列
     * 表示所有网络接口接收到的数据总量
     * </p>
     * <p>
     * 单位：字节 (bytes)
     * </p>
     */
    private long rxBytes;

    /**
     * 发送的总字节数（上传流量）
     * <p>
     * 数据来源：/proc/net/dev 第 9 列
     * 表示所有网络接口发送的数据总量
     * </p>
     * <p>
     * 单位：字节 (bytes)
     * </p>
     */
    private long txBytes;

    /**
     * 接收的数据包总数
     * <p>
     * 数据来源：/proc/net/dev 第 2 列
     * 表示所有网络接口成功接收的数据包数量
     * </p>
     */
    private long rxPackets;

    /**
     * 发送的数据包总数
     * <p>
     * 数据来源：/proc/net/dev 第 10 列
     * 表示所有网络接口成功发送的数据包数量
     * </p>
     */
    private long txPackets;

    /**
     * 接收错误总数
     * <p>
     * 数据来源：/proc/net/dev 第 3 列
     * 表示所有网络接口在接收数据时发生的错误包数量
     * 该值过高可能表示网络连接问题或硬件故障
     * </p>
     */
    private long rxErrors;

    /**
     * 发送错误总数
     * <p>
     * 数据来源：/proc/net/dev 第 11 列
     * 表示所有网络接口在发送数据时发生的错误包数量
     * 该值过高可能表示网络拥塞或驱动问题
     * </p>
     */
    private long txErrors;
}
