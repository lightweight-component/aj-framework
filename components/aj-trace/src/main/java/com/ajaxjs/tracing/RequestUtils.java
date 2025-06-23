package com.ajaxjs.tracing;


import com.ajaxjs.tracing.model.HeaderInfo;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HttpServletRequest请求类
 *
 * @author Emily
 */
public class RequestUtils {
    /**
     * unknown
     */
    private static final String UNKNOWN = "unknown";

    /**
     * 本地IP
     */
    private static final String LOCAL_IP = "127.0.0.1";

    /**
     * IP地址0:0:0:0:0:0:0:1是一个IPv6地址，通常用于表示本地回环地址（localhost）
     */
    private static final String LOCAL_HOST = "0:0:0:0:0:0:0:1";

    /**
     * 是否是内网正则表达式
     */
    private static final String INTERNET = "^(127\\.0\\.0\\.1)|(localhost)|(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(172\\.((1[6-9])|(2\\d)|(3[01]))\\.\\d{1,3}\\.\\d{1,3})|(192\\.168\\.\\d{1,3}\\.\\d{1,3})$";

    /**
     * 服务器IP
     */
    private static String SERVER_IP = null;

    /**
     * 获取请求真实IP,支持代理，如：179.156.81.168, 10.171.10.12，则取第一个IP 179.156.81.168
     *
     * @return 真实IP
     */
    public static String getRealClientIp() {
        String ip = getClientIp();
        return ip.contains(",") ? StringUtils.split(ip, ",")[0] : ip;
    }

    /**
     * 获取客户端IP
     *
     * @return 客户端IP
     */
    public static String getClientIp() {
        if (isServlet())
            return getClientIp(getRequest());

        return LOCAL_IP;
    }

    /**
     * 获取客户单IP地址
     *
     * @param request 请求对象
     * @return 客户端IP
     */
    public static String getClientIp(HttpServletRequest request) {
        try {
            String ip = request.getHeader(HeaderInfo.X_FORWARDED_FOR);
            if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader(HeaderInfo.PROXY_CLIENT_IP);
            }
            if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader(HeaderInfo.WL_PROXY_CLIENT_IP);
            }
            if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader(HeaderInfo.HTTP_CLIENT_IP);
            }
            if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader(HeaderInfo.HTTP_X_FORWARDED_FOR);
            }
            if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            if (LOCAL_HOST.equalsIgnoreCase(ip))
                ip = LOCAL_IP;

            return ip;
        } catch (Exception exception) {
            return "";
        }
    }

    /**
     * 判定是否是内网地址
     *
     * @param ip IP地址
     * @return 是否内网
     */
    public static boolean isInternet(String ip) {
        if (StrUtil.isEmptyText(ip))
            return false;

        if (LOCAL_HOST.equals(ip))
            return true;

        Pattern reg = Pattern.compile(INTERNET);
        Matcher match = reg.matcher(ip);
        return match.find();
    }

    /**
     * 获取服务器端的IP
     *
     * @return 服务器端IP
     */
    public static String getServerIp() {
        if (StringUtils.hasText(SERVER_IP))
            return SERVER_IP;

        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                String name = netInterface.getName();

                if (!name.contains("docker") && !name.contains("lo")) {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress ip = addresses.nextElement();
                        //loopback地址即本机地址，IPv4的loopback范围是127.0.0.0 ~ 127.255.255.255
                        if (ip != null && !ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")) {
                            SERVER_IP = ip.getHostAddress();
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            SERVER_IP = LOCAL_IP;
        }
        return SERVER_IP;
    }

    public static void setServerIp(String serverIp) {
        SERVER_IP = serverIp;
    }

    /**
     * 是否存在servlet上下文
     *
     * @return 是否servlet上下文
     */
    public static boolean isServlet() {
        return RequestContextHolder.getRequestAttributes() != null;
    }

    /**
     * 获取用户当前请求的HttpServletRequest
     *
     * @return 请求对象
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        assert attributes != null;
        return attributes.getRequest();
    }

    /**
     * 获取当前请求的HttpServletResponse
     *
     * @return 响应对象
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        assert attributes != null;
        return attributes.getResponse();
    }

    /**
     * 获取请求头，请求头必须传
     *
     * @param header 请求头
     * @return 请求头结果
     */
    public static String getHeader(String header) {
        return getHeader(header, false);
    }

    /**
     * 获取请求头，请求头不存在则返回默认值
     *
     * @param header       请求头名
     * @param defaultValue 默认值
     * @return 请求头
     */
    public static String getHeaderOrDefault(String header, String defaultValue) {
        String value = getHeader(header, false);
        if (value == null || value.isEmpty() || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取请求头
     *
     * @param header   请求头
     * @param required 是否允许请求头为空或者不传递，true-是；false-否
     * @return 请求头结果
     */
    public static String getHeader(String header, boolean required) {
        if (header == null || header.isEmpty() || !isServlet()) {
            return null;
        }
        String value = getRequest().getHeader(header);
        if (!required) {
            return value;
        }
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("非法参数");
        }
        return value;
    }

    /**
     * 获取请求头
     *
     * @param request 请求servlet对象
     * @return 请求头集合对象
     */
    public static Map<String, Object> getHeaders(HttpServletRequest request) {
        Assert.notNull(request, "Illegal Parameter: request must not be null");
        Map<String, Object> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        Optional.ofNullable(headerNames).ifPresent(headerName -> {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = request.getHeader(name);
                headers.put(name, value);
            }
        });
        return headers;
    }

    /**
     * 获取Get、POST等URL后缀请求参数
     *
     * @param request 请求上下文
     * @return 参数集合
     */
    public static Map<String, Object> getParameters(HttpServletRequest request) {
        Assert.notNull(request, "Illegal Parameter: request must not be null");
        Enumeration<String> names = request.getParameterNames();
        if (names == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> paramMap = new LinkedHashMap<>();
        while (names.hasMoreElements()) {
            String key = names.nextElement();
            if (!paramMap.containsKey(key)) {
                paramMap.put(key, request.getParameter(key));
            }
        }
        return paramMap;
    }
}
