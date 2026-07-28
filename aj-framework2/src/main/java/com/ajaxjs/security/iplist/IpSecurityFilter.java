package com.ajaxjs.security.iplist;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * IP 安全过滤器
 * 功能：IP 黑名单拦截、访问频率限制、可疑请求检测
 */
@Slf4j
public class IpSecurityFilter implements Filter {
    // IP 黑名单（线程安全）
    private final Set<String> blacklistedIps = ConcurrentHashMap.newKeySet();

    // 访问频率限制缓存（key=IP，value=频率限制信息）
    private final ConcurrentMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    /**
     * 过滤器核心逻辑
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param chain    过滤器链
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String clientIp = IpUtils.getClientRealIp(httpRequest); // 获取客户端真实 IP



        // 1. 黑名单校验：命中则拦截
        if (blacklistedIps.contains(clientIp)) {
            logSecurityEvent("IP 黑名单拦截", clientIp, httpRequest);
            sendErrorResponse(httpResponse, 403, "您的 IP 已被禁止访问");
            return;
        }

        // 2. 频率限制校验：访问过频则拦截
        if (isRateLimited(clientIp)) {
            logSecurityEvent("频率限制拦截", clientIp, httpRequest);
            sendErrorResponse(httpResponse, 429, "访问过于频繁，请稍后再试");
            return;
        }

        // 3. 可疑请求校验：检测异常行为
        if (isSuspiciousRequest(clientIp, httpRequest)) {
            logSecurityEvent("可疑请求拦截", clientIp, httpRequest);
            blacklistedIps.add(clientIp); // 加入黑名单
            sendErrorResponse(httpResponse, 403, "检测到异常访问行为，IP 已被限制");
            return;
        }

        // 所有校验通过，放行请求
        chain.doFilter(request, response);
    }

    /**
     * 频率限制校验
     * 规则：1分钟内最多60次请求
     *
     * @param ip 客户端 IP
     * @return true=触发限制，false=未触发
     */
    private boolean isRateLimited(String ip) {
        // 不存在则初始化频率信息
        RateLimitInfo info = rateLimitMap.computeIfAbsent(ip, k -> new RateLimitInfo());
        long currentTime = System.currentTimeMillis();

        if (currentTime - info.getWindowStart() > 60000)  // 时间窗口过期（超过1分钟），重置计数
            info.reset(60, currentTime);

        return !info.tryAcquire();  // 尝试获取令牌：无令牌则触发限制
    }

    /**
     * 可疑请求检测
     * 规则：无 User-Agent、访问敏感路径视为可疑
     *
     * @param ip      客户端 IP
     * @param request 请求对象
     * @return true=可疑，false=正常
     */
    private boolean isSuspiciousRequest(String ip, HttpServletRequest request) {
        // 1. 无 User-Agent 视为可疑
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.trim().isEmpty())
            return true;

        // 2. 访问敏感路径视为可疑（后台管理、数据库管理等）
        String uri = request.getRequestURI().toLowerCase();
        return uri.contains("admin") || uri.contains("phpmyadmin") || uri.contains("wp-admin") || uri.contains("shell");// 无异常行为
    }

    /**
     * 发送错误响应
     *
     * @param response 响应对象
     * @param status   状态码
     * @param message  错误信息
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\": " + status + ", \"message\": \"" + message + "\"}");
    }

    /**
     * 记录安全事件日志
     *
     * @param event   事件类型
     * @param ip      客户端 IP
     * @param request 请求对象
     */
    private void logSecurityEvent(String event, String ip, HttpServletRequest request) {
        log.warn("安全事件触发 - 类型: {}, IP: {}, URI: {}, User-Agent: {}", event, ip, request.getRequestURI(), request.getHeader("User-Agent"));
    }

    /**
     * 频率限制信息封装
     * 内部类：记录令牌数、时间窗口起始时间
     */
    private static class RateLimitInfo {
        // 剩余令牌数（每请求消耗1个）
        private int tokens;
        // 时间窗口起始时间
        private long windowStart;
        // 最大令牌数（1分钟60个）
        private final int maxTokens = 60;

        /**
         * 初始化：默认填充最大令牌，时间窗口为当前时间
         */
        RateLimitInfo() {
            reset(maxTokens, System.currentTimeMillis());
        }

        /**
         * 重置频率限制信息
         *
         * @param tokens      令牌数
         * @param windowStart 时间窗口起始时间
         */
        void reset(int tokens, long windowStart) {
            this.tokens = tokens;
            this.windowStart = windowStart;
        }

        /**
         * 尝试获取令牌
         *
         * @return true=获取成功，false=无令牌
         */
        boolean tryAcquire() {
            if (tokens > 0) {
                tokens--;
                return true;
            }

            return false;
        }

        // 获取时间窗口起始时间
        long getWindowStart() {
            return windowStart;
        }
    }
}