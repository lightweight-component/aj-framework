package com.ajaxjs.spring.traceid;

import com.ajaxjs.util.BoxLogger;
import com.ajaxjs.util.StrUtil;
import com.ajaxjs.util.http_request.model.HttpConstants;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Adds TraceId on every single Request, and adds ContentCachingRequestWrapper/ContentCachingResponseWrapper
 */
@WebFilter("/**")
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)  // 比最高优先级稍低，避免冲突
public class TraceXFilter implements Filter {
    private final static String X_TRACE = "x-trace";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String traceId = req.getHeader(X_TRACE);

        if (!StringUtils.hasLength(traceId))
            traceId = UUID.randomUUID().toString().replace("-", StrUtil.EMPTY_STRING).toUpperCase();

        MDC.put(BoxLogger.TRACE_KEY, traceId);

        // 包装请求，缓存 body
        // Spring 的无效
//        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(req);
//        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);
        if (!HttpConstants.GET.equals(req.getMethod()) && StrUtil.hasText(request.getContentType()) && request.getContentType().contains(HttpConstants.CONTENT_TYPE_JSON))
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

        return StrUtil.EMPTY_STRING;
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