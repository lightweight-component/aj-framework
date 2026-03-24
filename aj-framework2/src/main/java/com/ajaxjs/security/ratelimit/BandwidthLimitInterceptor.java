package com.ajaxjs.security.ratelimit;

import com.ajaxjs.security.iplist.IpList;
import com.ajaxjs.security.ratelimit.annotation.BandwidthLimit;
import com.ajaxjs.security.ratelimit.annotation.BandwidthUnit;
import com.ajaxjs.security.ratelimit.annotation.LimitType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 带宽限速拦截器
 * <p>
 * 在 preHandle 中包装响应，在 afterCompletion 中关闭
 */
@Slf4j
public class BandwidthLimitInterceptor implements HandlerInterceptor {

    private final BandwidthLimitManager limitManager = new BandwidthLimitManager();

    private static final String WRAPPED_RESPONSE_ATTR = "BandwidthLimitWrappedResponse";
    private static final String ORIGINAL_RESPONSE_ATTR = "BandwidthLimitOriginalResponse";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod))
            return true;

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        BandwidthLimit annotation = handlerMethod.getMethodAnnotation(BandwidthLimit.class);

        // 如果方法没有注解，检查类级别的注解
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), BandwidthLimit.class);
        }

        if (annotation != null) {
            String path = request.getRequestURI();
            log.info("========== Interceptor: Found @BandwidthLimit for path: {}, type: {}, value: {} {}/s ==========",
                    path, annotation.type(), annotation.value(), annotation.unit());

            // 获取带宽参数
            LimitType type = annotation.type();
            long bandwidth = calculateBandwidth(request, annotation);
            long bandwidthBytesPerSecond = annotation.unit().toBytesPerSecond(bandwidth);
            long capacity = (long) (bandwidthBytesPerSecond * annotation.capacityMultiplier());
            String key = null;

            switch (type) {
                case GLOBAL -> key = "global";
                case API -> key = path;
                case USER -> {
                    String userId = request.getHeader(annotation.userHeader());
                    key = userId != null ? userId : request.getRemoteAddr();
                }
                case IP -> key = IpList.getClientIp(request);
            }

            // 获取或创建令牌桶
            TokenBucket bucket = limitManager.getBucket(type, key, capacity, bandwidthBytesPerSecond);

            log.info("Interceptor: Token bucket created - type={}, key={}, capacity={}/s, rate={}/s",
                    type, key, BandwidthUnit.formatBytes(capacity), BandwidthUnit.formatBytes(bandwidthBytesPerSecond));

            // 设置响应头到原始响应（这样浏览器才能看到）
            response.setHeader("X-Bandwidth-Limit", BandwidthUnit.formatBytes(bandwidthBytesPerSecond) + "/s");
            response.setHeader("X-Bandwidth-Type", type.name());
            response.setHeader("X-Bandwidth-Key", key);
            response.setHeader("X-Bandwidth-Capacity", BandwidthUnit.formatBytes(capacity));

            log.info("Interceptor: Response headers set - X-Bandwidth-Limit={}",
                    BandwidthUnit.formatBytes(bandwidthBytesPerSecond) + "/s");

            // 创建限速响应包装器（传入共享的 TokenBucket）
            BandwidthLimitResponseWrapper wrappedResponse = new BandwidthLimitResponseWrapper(
                    response, bucket, bandwidthBytesPerSecond, annotation.chunkSize());

            // 将包装器保存到请求中
            request.setAttribute(WRAPPED_RESPONSE_ATTR, wrappedResponse);
            request.setAttribute(ORIGINAL_RESPONSE_ATTR, response);
            request.setAttribute("BandwidthLimit", annotation);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理资源
        BandwidthLimitResponseWrapper wrappedResponse = (BandwidthLimitResponseWrapper) request.getAttribute(WRAPPED_RESPONSE_ATTR);
        if (wrappedResponse != null) {
            try {
                wrappedResponse.close();
            } catch (Exception e) {
                log.error("Error closing wrapped response", e);
            }
        }
    }

    private static long calculateBandwidth(HttpServletRequest request, BandwidthLimit annotation) {
        if (annotation.free() > 0 || annotation.vip() > 0) {
            String userType = request.getHeader("X-User-Type");

            if ("vip".equalsIgnoreCase(userType))
                return annotation.vip() > 0 ? annotation.vip() : annotation.value();
            else if ("free".equalsIgnoreCase(userType))
                return annotation.free() > 0 ? annotation.free() : annotation.value();
        }

        return annotation.value();
    }
}