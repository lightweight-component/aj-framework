package com.ajaxjs.security.iplist;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * IP 调试控制器
 * 功能：提供接口查看 IP 相关信息，验证获取逻辑
 */
@RestController
public class IpDebugController {
    /**
     * 调试 IP 获取结果
     *
     * @param request 请求对象
     * @return IP 相关信息
     */
    @GetMapping("/debug/ip")
    public Map<String, Object> debugIp(HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        // 核心：真实客户端 IP
        result.put("真实客户端 IP", IpUtils.getClientRealIp(request));
        // 对比：原生 RemoteAddr
        result.put("RemoteAddr", request.getRemoteAddr());
        // 各 IP 头字段原始值
        result.put("X-Forwarded-For", request.getHeader("X-Forwarded-For"));
        result.put("X-Real-IP", request.getHeader("X-Real-IP"));
        result.put("Proxy-Client-IP", request.getHeader("Proxy-Client-IP"));
        result.put("WL-Proxy-Client-IP", request.getHeader("WL-Proxy-Client-IP"));
        // 其他请求信息
        result.put("请求方法", request.getMethod());
        result.put("请求 URI", request.getRequestURI());
        result.put("User-Agent", request.getHeader("User-Agent"));

        return result;
    }

    /**
     * 获取所有 IP 相关头字段
     *
     * @param request 请求对象
     * @return IP 头字段键值对
     */
    @GetMapping("/debug/ip-headers")
    public Map<String, String> getAllIpHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        String[] ipHeaders = {// 常见 IP 相关头字段列表
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"
        };

        for (String header : ipHeaders) {// 遍历获取非空头字段
            String value = request.getHeader(header);
            if (value != null && !value.trim().isEmpty())
                headers.put(header, value);
        }

        return headers;
    }
}