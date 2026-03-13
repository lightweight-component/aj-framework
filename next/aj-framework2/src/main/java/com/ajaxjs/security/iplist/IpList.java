package com.ajaxjs.security.iplist;

import com.ajaxjs.security.InterceptorAction;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Simply check the IP
 */
@Data
@Component
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "security.IpList.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "security.ip-list")
public class IpList extends InterceptorAction<IpListCheck> {
    /**
     * The ip allows to access
     */
    private List<String> whiteList;

    /**
     * The ip disallows to access
     */
    private List<String> blackList;

    @Override
    public boolean action(IpListCheck annotation, HttpServletRequest req) {
        String ip = IpList.getClientIp(req);

        if (!ObjectHelper.isEmpty(whiteList) && StringUtils.hasText(ip) && !whiteList.contains(ip))
            throw new SecurityException("Invalid IP:[" + ip + "] access");

        if (!ObjectHelper.isEmpty(blackList) && StringUtils.hasText(ip) && !blackList.contains(ip))
            throw new SecurityException("Invalid IP:[" + ip + "] access");

        return true;
    }

    final static String UNKNOWN = "unknown";

    /**
     * The ip from browser.
     * 要外网访问才能获取到外网地址，如果你在局域网甚至本机上访问，获得的是内网或者本机的ip
     *
     * @param request The request object
     * @return The ip
     */
    public static String getClientIp(HttpServletRequest request) {
        String ipAddress = null;

        String ipAddresses = request.getHeader("X-Forwarded-For"); //X-Forwarded-For：Squid 服务代理

        if (ObjectHelper.isEmptyText(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses))
            ipAddresses = request.getHeader("X-Real-IP");   // X-Real-IP：nginx服务代理

        if (ObjectHelper.isEmptyText(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses))
            ipAddresses = request.getHeader("Proxy-Client-IP"); // Proxy-Client-IP：apache 服务代理

        if (ObjectHelper.isEmptyText(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses))
            ipAddresses = request.getHeader("HTTP_CLIENT_IP"); // HTTP_CLIENT_IP：有些代理服务器

        // 有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ObjectHelper.hasText(ipAddresses))
            ipAddress = ipAddresses.split(",")[0];

        if (ObjectHelper.isEmptyText(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses))
            ipAddress = request.getRemoteAddr();

        return ipAddress;
    }
}
