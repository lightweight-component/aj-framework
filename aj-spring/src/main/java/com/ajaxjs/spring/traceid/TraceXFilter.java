package com.ajaxjs.spring.traceid;

import com.ajaxjs.util.BoxLogger;
import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.RandomTools;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Adds TraceId on every single Request, and adds ContentCachingRequestWrapper/ContentCachingResponseWrapper
 */
@WebFilter("/**")
@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1)  // 比最高优先级稍低，避免冲突
public class TraceXFilter implements Filter {
    private final static String X_TRACE = "x-trace";

    private final static String CONTENT_TYPE_JSON = "application/json";

    private final static String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    private final static String GET = "GET";
    private final static String POST = "POST";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String traceId = req.getHeader(X_TRACE);

        if (!StringUtils.hasLength(traceId))
//            traceId = UUID.randomUUID().toString().replace("-", StrUtil.EMPTY_STRING).toUpperCase();
            traceId = RandomTools.uuid().toString();

        MDC.put(BoxLogger.TRACE_KEY, traceId);
        String contentType = request.getContentType();

        if (!GET.equals(req.getMethod()) && ObjectHelper.hasText(contentType)) {
            HttpServletRequestWrapper wrappedRequest = null;

            if (POST.equals(req.getMethod()) && contentType.contains(CONTENT_TYPE_FORM)) {
                wrappedRequest = new ContentCachingRequestWrapper(req);
                /* Manually trigger stream */
//            wrappedRequest.getParameterNames();
//            System.out.println("Request Body: " + getStreamBodyAsStr(wrappedRequest));
            } else if (contentType.contains(CONTENT_TYPE_FORM) || contentType.contains(CONTENT_TYPE_JSON))
                wrappedRequest = new BufferedRequestWrapper(req);

            chain.doFilter(wrappedRequest == null ? req : wrappedRequest, response);
        } else
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

    /**
     * Get the stream data as string from request body.
     * This method is to replace:
     * <p>
     * try (ServletInputStream inputStream = req.getInputStream()) {
     * raw = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
     * } catch (IOException e) {
     * throw new RuntimeException(e);
     * }
     * <p>
     * Since ContentCachingRequestWrapper is used.
     *
     * @return The raw body
     */
    public static String getStreamBodyAsStr(HttpServletRequest req) {
        if (req instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrappedRequest = (ContentCachingRequestWrapper) req;
            byte[] cachedBody = wrappedRequest.getContentAsByteArray();

            try {
                return new String(cachedBody, wrappedRequest.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                log.warn("UnsupportedEncodingException", e);
                throw new RuntimeException(e);
            }
        } else {
            try (ServletInputStream inputStream = req.getInputStream()) {
                return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.warn("getStreamBodyAsStr", e);
                throw new RuntimeException(e);
            }
        }
    }
}