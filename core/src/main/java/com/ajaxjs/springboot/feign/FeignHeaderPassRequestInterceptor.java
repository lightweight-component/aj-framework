package com.ajaxjs.springboot.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Title 透传header
 * Description
 */
public class FeignHeaderPassRequestInterceptor implements RequestInterceptor {
    private final List<String> skipHeaders =
            Arrays.asList("host", ":authority", ":method", ":path", ":scheme", "accept", "accept-encoding", "accept-language",
                    "cache-control", "content-length", "content-type", "origin", "pragma", "referer", "sec-fetch-dest",
                    "sec-fetch-mode", "sec-fetch-site", "user-agent");

    /**
     * 复写feign请求对象
     *
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        if (httpServletRequest == null) {
            return;
        }
        // 获取请求头
        Map<String, String> headers = getHeaders(httpServletRequest);
        if (headers != null) {
            for (String headerName : headers.keySet()) {
                if (!skipHeaders.contains(headerName.toLowerCase()))
                    requestTemplate.header(headerName, getHeaders(getHttpServletRequest()).get(headerName));
            }
        }
    }

    // 获取请求对象
    private HttpServletRequest getHttpServletRequest() {
        try {
            if (RequestContextHolder.getRequestAttributes() != null) {
                return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, String> getHeaders(HttpServletRequest request) {
        if (request == null)
            return null;

        Map<String, String> map = new LinkedHashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();

        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

}
