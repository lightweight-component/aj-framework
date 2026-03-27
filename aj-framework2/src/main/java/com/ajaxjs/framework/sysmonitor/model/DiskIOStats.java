package com.ajaxjs.framework.sysmonitor.model;

import lombok.Data;

/**
 * Linux 磁盘 IO 统计信息
 * <p>
 * 数据来源：/proc/diskstats
 * 该文件包含了所有磁盘设备的 IO 统计信息，用于监控磁盘性能和 I/O 负载
 * </p>
 */
@Data
public class DiskIOStats {

    /**
     * 读取的总字节数
     * <p>
     * 计算方式：从 /proc/diskstats 第 6 列获取读取的扇区数，乘以 512 字节
     * </p>
     * <p>
     * 单位：字节 (bytes)
     * </p>
     */
    private long readBytes;

    /**
     * 写入的总字节数
     * <p>
     * 计算方式：从 /proc/diskstats 第 10 列获取写入的扇区数，乘以 512 字节
     * </p>
     * <p>
     * 单位：字节 (bytes)
     * </p>
     */
    private long writeBytes;

    /**
     * 读取操作总次数
     * <p>
     * 数据来源：/proc/diskstats 第 4 列
     * 表示所有磁盘设备完成的读取 I/O 请求数量
     * </p>
     */
    private long readOps;

    /**
     * 写入操作总次数
     * <p>
     * 数据来源：/proc/diskstats 第 8 列
     * 表示所有磁盘设备完成的写入 I/O 请求数量
     * </p>
     */
    private long writeOps;
}
