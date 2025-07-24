package com.ajaxjs.business.net;


import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 根据IP统计访问次数
 */
public class IpVisitCount {
    public static final String SEMICOLON = ";";

    // Nginx代理传递的实际客户端 IP-header
    public static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "X-REAL-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR",
            "REMOTE-HOST"
    };

    /**
     * 获取客户端的IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);

            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip))
                return ip;
        }

        return request.getRemoteAddr();
    }

    /**
     * 访问计数器Map<IP地址, 次数>
     */
    private static final Map<String, AtomicInteger> visitCounterMap = new ConcurrentHashMap<>();

    /**
     * 增加并获取最新的访问次数
     */
    private static int incrementCounter(String clientIp) {
        AtomicInteger visitCounter = visitCounterMap.get(clientIp);

        if (null == visitCounter) {
            visitCounter = new AtomicInteger();
            AtomicInteger oldValue = visitCounterMap.putIfAbsent(clientIp, visitCounter);

            if (null != oldValue)  // 使用 putIfAbsent 时注意: 判断是否有并发导致的原有值。
                visitCounter = oldValue;
        }

        return visitCounter.incrementAndGet();
    }

    /**
     * 清除某个IP的访问次数
     */
    private static int clearCounter(String clientIp) {
        visitCounterMap.remove(clientIp);

        return 0;
    }

    private static final String CONST_PARAM_NAME_ACTION = "action";

    private static final String CONST_ACTION_VALUE_CLEAR = "clear";

    private static final String CONST_PARAM_NAME_FORMAT = "format";

    private static final String CONST_FORMAT_VALUE_JSON = "json";

    private static final String CONST_ATTR_NAME_CLIENTIP = "clientIp";

    private static final String CONST_ATTR_NAME_VISITCOUNT = "visitCount";

    public static void count(HttpServletRequest request) {
        // 获取客户端IP地址
        String clientIp = getClientIp(request);
        int visitCount = 0;

        if (clientIp != null) // 获取访问次数
            visitCount = incrementCounter(clientIp);

        // 如果需要清空数据
        String action = request.getParameter(CONST_PARAM_NAME_ACTION);

        if (CONST_ACTION_VALUE_CLEAR.equalsIgnoreCase(action))
            visitCount = clearCounter(clientIp);

        // 如果需要返回JSON格式的数据
        String format = request.getParameter(CONST_PARAM_NAME_FORMAT);

        if (CONST_FORMAT_VALUE_JSON.equalsIgnoreCase(format)) {
            // 返回JSON
            Map<String, Object> result = new HashMap<>();
            result.put(CONST_ATTR_NAME_CLIENTIP, clientIp);
            result.put(CONST_ATTR_NAME_VISITCOUNT, visitCount);
        }
    }

}
