package com.ajaxjs.business.utils;


import com.ajaxjs.business.net.Ip;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * web集群全局唯一request id生成算法, 替代uuid等“通用”方案
 */
public class UnionRequestIdGen {
    private static final AtomicLong LAST_ID = new AtomicLong(); // 自增id，用于requestId的生成过程
    private static final long START_TIMESTAMP = System.currentTimeMillis(); // 启动加载时的时间戳，用于requestId的生成过程
    private static final String IP = Ip.getLocalIp(); // 本机ip地址，用于requestId的生成过程

    /**
     * 规则： hexIp(ip)base36(timestamp)-seq
     *
     * @return ID
     */
    public static String resolveReqId() {
        return hexIp(IP) + "-" + Long.toString(START_TIMESTAMP, Character.MAX_RADIX) + "-" + LAST_ID.incrementAndGet();
    }

    /**
     * 将ip转换为定长8个字符的16进制表示形式：255.255.255.255 -> FFFFFFFF
     *
     * @param ip 字符串形式的IP地址，格式为"xxx.xxx.xxx.xxx"。
     * @return 返回转换后的16进制字符串，长度为8个字符。
     */
    static String hexIp(String ip) {
        StringBuilder sb = new StringBuilder();

        for (String seg : ip.split("\\.")) {     // 分割IP地址的每个段落
            String h = Integer.toHexString(Integer.parseInt(seg)); // 将每个段落转换为16进制
            // 如果16进制长度为1，则在前面补0，以保证每个段落转换后的长度至少为2
            if (h.length() == 1)
                sb.append("0");

            sb.append(h);
        }

        return sb.toString().toUpperCase(); // 确保输出是大写的16进制字符
    }

    /**
     * 将定长8个字符的16进制字符串转换回原始的IP地址。
     *
     * @param hexStr 8个字符的16进制字符串。
     * @return 返回原始的IP地址字符串，格式为"xxx.xxx.xxx.xxx"。
     */
    static String decodeHexIp(String hexStr) {
        if (hexStr == null || hexStr.length() != 8)
            throw new IllegalArgumentException("Invalid input, must be an 8-character hexadecimal string.");

        StringBuilder sb = new StringBuilder();
        // 每两个字符代表一个字节
        for (int i = 0; i < hexStr.length(); i += 2) {
            int decimal = Integer.parseInt(hexStr.substring(i, i + 2), 16); // 取出两位16进制字符，并转换成整数
            sb.append(decimal);

            if (i < hexStr.length() - 2) // 如果不是最后一个段落，则添加点号分隔符
                sb.append(".");
        }

        return sb.toString();
    }

    public static void getInfo(String id) {
        String[] arr = id.split("-");
        String ip = decodeHexIp(arr[0]);
        System.out.println(ip);

        long decodedTimestamp = Long.parseLong(arr[1], Character.MAX_RADIX);

        System.out.println(decodedTimestamp);

        Date date = new Date(decodedTimestamp);
        System.out.println("Date: " + date);
    }

}
