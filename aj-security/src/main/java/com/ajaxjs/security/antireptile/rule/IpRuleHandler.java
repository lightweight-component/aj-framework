package com.ajaxjs.security.antireptile.rule;

import com.ajaxjs.security.antireptile.config.AntiReptileProperties;
import com.ajaxjs.security.antireptile.util.LocalCache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 基于单机缓存的 IP 访问频率规则。
 * <p>按 URI 和 IP 统计固定窗口内的请求，保留原实现首个请求只初始化的计数约定。
 * 超限后按 IP 锁定，锁定期间访问任何受保护 URI 都会命中且不续期。
 * 命中时清除该 IP 所有 URI 的计数，确保锁定到期后可重新统计；验证码通过可提前解锁。</p>
 * <p>检查及重置使用同一实例锁，保证初始化、递增和锁定的复合操作在单机并发下原子执行。</p>
 */
@Slf4j
public class IpRuleHandler extends AbstractRule {
    /** 当前应用实例私有的有界过期缓存。 */
    @Autowired
    private LocalCache localCache;

    /** IP 白名单、窗口、阈值和锁定秒数配置。 */
    @Autowired
    private AntiReptileProperties properties;

    /**
     * 检查白名单、IP 锁定状态及 URI 请求频率。
     * @param request 用于提取 IP 和 URI 的业务请求
     * @param response 当前响应，此规则不直接写入
     * @return 锁定中或当前请求超限时为 true，否则为 false
     */
    @Override
    protected synchronized boolean doExecute(HttpServletRequest request, HttpServletResponse response) {
        String ip = getIpAddr(request);
        List<String> ignoreIps = properties.getIpRule().getIgnoreIp();

        if (ignoreIps != null) {
            for (String ignored : ignoreIps) {
                String prefix = ignored.endsWith("*") ? ignored.substring(0, ignored.length() - 1) : ignored;

                if (ip.startsWith(prefix))
                    return false;
            }
        }

        if (localCache.get("ip-lock:" + ip, String.class) != null)
            return true;

        String uri = request.getRequestURI();
        String key = windowKey(ip, uri);
        Window window = localCache.get(key, Window.class);

        if (window == null) {
            localCache.put(key, new Window(), properties.getIpRule().getExpirationTime().longValue());

            return false;
        }

        if (++window.count > properties.getIpRule().getRequestMaxSize()) {
            long seconds = properties.getIpRule().getLockExpire();
            localCache.put("ip-lock:" + ip, uri, Math.multiplyExact(seconds, 1000L));
            localCache.removeByPrefix(windowPrefix(ip));
            log.info("Intercepted request, uri: {}, ip: {}. Automatically unlock after {} seconds", uri, ip, seconds);

            return true;
        }

        return false;
    }

    /**
     * 清除原 URI 的计数和当前 IP 的锁定，允许重新开始统计。
     * @param request 验证码请求，用于获取客户端 IP
     * @param realRequestUri 原先被拦截的业务 URI
     */
    @Override
    public synchronized void reset(HttpServletRequest request, String realRequestUri) {
        String ip = getIpAddr(request);
        localCache.remove(windowKey(ip, realRequestUri));
        localCache.remove("ip-lock:" + ip);
    }

    /** 使用 IP 长度分隔 IP 与 URI，避免简单拼接引起键冲突。 */
    private static String windowKey(String ip, String uri) {
        return windowPrefix(ip) + uri;
    }

    /** 获取指定 IP 的独立窗口命名空间，供锁定时统一清理。 */
    private static String windowPrefix(String ip) {
        return "ip-window:" + ip.length() + ":" + ip;
    }

    /** 固定窗口内后续请求的计数，更新时不重新写缓存以避免窗口续期。 */
    private static class Window {
        private long count;
    }

    /**
     * 按顺序从代理请求头和远端地址提取客户端地址。
     * <p>依次检查 {@code x-forwarded-for}、{@code Proxy-Client-IP}、
     * {@code WL-Proxy-Client-IP}，忽略空值和 unknown 后回退到远端地址。
     * 请求头原样返回，不拆分多级代理地址，也不校验可信代理，部署方需保证上游头部来源可靠。</p>
     *
     * @param request 当前 HTTP 请求
     * @return 第一个有效请求头的原始内容，或请求的远端地址
     */
    private static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("Proxy-Client-IP");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("WL-Proxy-Client-IP");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();

        return ip;
    }

    /**
     * 指定内置 IP 规则的执行优先级。
     *
     * @return {@code 0}，默认先于 User-Agent 规则执行
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
