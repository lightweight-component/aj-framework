package com.ajaxjs.security.iplist;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * IP 工具类
 * 功能：安全获取客户端真实 IP，过滤内网 IP、伪造 IP，兼容多级代理场景
 * 适用：Spring Boot/Spring MVC 项目
 */
public class IpUtils {

    // 未知 IP 标识
    private static final String UNKNOWN = "unknown";
    // 本地回环 IP（IPv4）
    private static final String LOCALHOST_IP = "127.0.0.1";
    // 本地回环 IP（IPv6）
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    // IP 分隔符（X-Forwarded-For 中多 IP 的分隔符）
    private static final String SEPARATOR = ",";

    // 内网 IP 段（需过滤的非公网 IP）
    private static final Set<String> INTERNAL_IP_SEGMENTS = new HashSet<>(Arrays.asList(
            "10.", "192.168.",
            "172.16.", "172.17.", "172.18.", "172.19.",
            "172.20.", "172.21.", "172.22.", "172.23.",
            "172.24.", "172.25.", "172.26.", "172.27.",
            "172.28.", "172.29.", "172.30.", "172.31."
    ));

    /**
     * 获取客户端真实公网 IP
     *
     * @param request HttpServletRequest 请求对象
     * @return 客户端真实 IP（优先公网 IP，无公网 IP 则返回内网/本地 IP）
     */
    public static String getClientRealIp(HttpServletRequest request) {
        // 1. 优先解析 X-Forwarded-For 头（核心字段）
        String ip = parseXForwardedFor(request.getHeader("X-Forwarded-For"));
        if (isValidPublicIp(ip))
            return ip;

        ip = getIpFromHeaders(request); // 2. 解析其他代理相关头字段
        if (isValidPublicIp(ip))
            return ip;

        ip = request.getRemoteAddr();// 3. 最后降级使用 getRemoteAddr（大概率是代理 IP）

        return LOCALHOST_IPV6.equals(ip) ? LOCALHOST_IP : ip;// 兼容 IPv6本地回环地址转换
    }

    /**
     * 解析 X-Forwarded-For 头，提取有效 IP
     * 逻辑：从后往前过滤内网 IP，优先返回第一个有效公网 IP；无公网 IP 则返回第一个有效 IP
     *
     * @param xffHeader X-Forwarded-For 头值
     * @return 解析后的 IP（null 表示无有效 IP）
     */
    private static String parseXForwardedFor(String xffHeader) {
        if (xffHeader == null || xffHeader.trim().isEmpty())  // 空值直接返回 null
            return null;

        String[] ips = xffHeader.split(SEPARATOR);// 按逗号分割多 IP

        for (int i = ips.length - 1; i >= 0; i--) {// 第一步：从后往前找第一个有效公网 IP（过滤内网 IP）
            String ip = ips[i].trim();

            if (isValidIp(ip) && !isInternalIp(ip))  // IP 格式合法 + 非内网 IP = 有效公网 IP
                return ip;
        }

        for (String ip : ips) {// 第二步：无公网 IP 时，返回第一个格式合法的 IP（可能是内网 IP）
            String trimmedIp = ip.trim();
            if (isValidIp(trimmedIp))
                return trimmedIp;
        }

        return null;  // 无任何有效 IP
    }

    /**
     * 从其他代理头中提取 IP
     *
     * @param request HttpServletRequest 请求对象
     * @return 提取到的 IP（null 表示无有效 IP）
     */
    private static String getIpFromHeaders(HttpServletRequest request) {
        // 常见的代理 IP 头字段列表
        String[] headers = {"X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};

        for (String header : headers) { // 遍历头字段，找到第一个有效 IP
            String ip = request.getHeader(header);
            if (isValidIp(ip))
                return ip;
        }

        return null;
    }

    /**
     * 校验 IP 是否为有效格式（排除 unknown、空值）
     *
     * @param ip 待校验 IP
     * @return true=有效，false=无效
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip) && isValidIpAddress(ip);
    }

    /**
     * 校验 IP 是否为有效公网 IP
     *
     * @param ip 待校验 IP
     * @return true=有效公网 IP，false=内网/本地/无效 IP
     */
    private static boolean isValidPublicIp(String ip) {
        return isValidIp(ip) && !isInternalIp(ip) && !isLocalhost(ip);
    }

    /**
     * 判断是否为内网 IP
     *
     * @param ip 待判断 IP
     * @return true=内网 IP，false=公网 IP
     */
    private static boolean isInternalIp(String ip) {
        if (ip == null)
            return false;

        return INTERNAL_IP_SEGMENTS.stream().anyMatch(ip::startsWith); // 匹配内网 IP 段前缀
    }

    /**
     * 判断是否为本地回环 IP
     *
     * @param ip 待判断 IP
     * @return true=本地 IP，false=非本地 IP
     */
    private static boolean isLocalhost(String ip) {
        return LOCALHOST_IP.equals(ip) || LOCALHOST_IPV6.equals(ip);
    }

    public static final String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    /**
     * 校验 IP 地址格式是否合法（支持 IPv4/IPv6）
     *
     * @param ip 待校验 IP
     * @return true=格式合法，false=格式非法
     */
    public static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty())
            return false;

        if (ip.matches(ipv4Pattern))// IPv4格式正则
            return true;

        // 简单判断 IPv6（包含冒号即认为合法，如需严格校验可补充正则）
        return ip.contains(":");// 其他格式均为非法
    }
}