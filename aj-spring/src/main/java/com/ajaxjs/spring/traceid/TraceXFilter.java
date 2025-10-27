package com.ajaxjs.spring.traceid;

import com.ajaxjs.util.*;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Adds TraceId on every single Request, and adds ContentCachingRequestWrapper/ContentCachingResponseWrapper
 */
@WebFilter("/**")
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)  // 比最高优先级稍低，避免冲突
public class TraceXFilter implements Filter {
    private final static String X_TRACE = "x-trace";

    private final static  String CONTENT_TYPE_JSON = "application/json";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String traceId = req.getHeader(X_TRACE);

        if (!StringUtils.hasLength(traceId))
//            traceId = UUID.randomUUID().toString().replace("-", StrUtil.EMPTY_STRING).toUpperCase();
            traceId = RandomTools.uuid().toString();

        MDC.put(BoxLogger.TRACE_KEY, traceId);

        // 包装请求，缓存 body
        // Spring 的无效
//        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(req);
//        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);
        if (!"GET".equals(req.getMethod()) && ObjectHelper.hasText(request.getContentType()) && request.getContentType().contains(CONTENT_TYPE_JSON))
            chain.doFilter(new BufferedRequestWrapper(req), response);
        else
            chain.doFilter(req, response); // GET 请求不记录
    }

    public static String getRequestBody(HttpServletRequest request) {
        if (request instanceof BufferedRequestWrapper) {
            BufferedRequestWrapper wrapper = (BufferedRequestWrapper) request;

            try {
                return StreamUtils.copyToString(wrapper.getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return CommonConstant.EMPTY_STRING;
    }

//    public static String getRequestBody(HttpServletRequest request) {
//        if (request instanceof ContentCachingRequestWrapper) {
//            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
//            byte[] contentAsByteArray = wrapper.getContentAsByteArray();
//
//            if (contentAsByteArray.length == 0) {
//                try {
//                    return StreamUtils.copyToString(wrapper.getInputStream(), StandardCharsets.UTF_8);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            return new String(contentAsByteArray, StandardCharsets.UTF_8);
//        }
//
//        return "";
//    }
}