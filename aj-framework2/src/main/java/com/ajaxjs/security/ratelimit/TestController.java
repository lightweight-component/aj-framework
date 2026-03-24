package com.ajaxjs.security.ratelimit;

import com.ajaxjs.security.ratelimit.annotation.BandwidthLimit;
import com.ajaxjs.security.ratelimit.annotation.BandwidthUnit;
import com.ajaxjs.security.ratelimit.annotation.LimitType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 提供各种限速维度的测试接口
 */
//@RestController
//@RequestMapping("/api")
public class TestController {

    @Autowired(required = false)
    private BandwidthLimitInterceptor bandwidthLimitInterceptor;

    @Autowired
    private BandwidthLimitManager bandwidthLimitManager;

    /**
     * 全局限速测试 - 200 KB/s
     */
    @BandwidthLimit(value = 200, unit = BandwidthUnit.KB, type = LimitType.GLOBAL)
    @GetMapping("/download/global")
    public void downloadGlobal(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpServletResponse limitedResponse = getLimitedResponse(request, response);
        limitedResponse.setContentType("application/octet-stream");
        limitedResponse.setHeader("Content-Disposition", "attachment; filename=global-limit-test.bin");
        limitedResponse.setHeader("X-Test-Type", "Global Limit");
        generateTestData(limitedResponse, 5 * 1024 * 1024); // 5MB
    }

    /**
     * API维度限速测试 - 500 KB/s
     */
    @BandwidthLimit(value = 500, unit = BandwidthUnit.KB, type = LimitType.API)
    @GetMapping("/download/api")
    public void downloadByApi(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpServletResponse limitedResponse = getLimitedResponse(request, response);
        limitedResponse.setContentType("application/octet-stream");
        limitedResponse.setHeader("Content-Disposition", "attachment; filename=api-limit-test.bin");
        limitedResponse.setHeader("X-Test-Type", "API Limit");
        generateTestData(limitedResponse, 5 * 1024 * 1024); // 5MB
    }

    /**
     * 用户维度限速测试 - 普通 200 KB/s，VIP 1 MB/s
     */
    @BandwidthLimit(value = 200, unit = BandwidthUnit.KB, type = LimitType.USER, free = 200, vip = 1024)
    @GetMapping("/download/user")
    public void downloadByUser(HttpServletRequest request,
                               @RequestHeader(value = "X-User-Type", defaultValue = "free") String userType,
                               @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId,
                               HttpServletResponse response) throws IOException {
        HttpServletResponse limitedResponse = getLimitedResponse(request, response);
        limitedResponse.setContentType("application/octet-stream");
        limitedResponse.setHeader("Content-Disposition", "attachment; filename=user-limit-test.bin");
        limitedResponse.setHeader("X-Test-Type", "User Limit - " + userType);
        limitedResponse.setHeader("X-User-Type", userType);
        limitedResponse.setHeader("X-User-Id", userId);
        generateTestData(limitedResponse, 5 * 1024 * 1024); // 5MB
    }

    /**
     * IP维度限速测试 - 300 KB/s
     */
    @BandwidthLimit(value = 300, unit = BandwidthUnit.KB, type = LimitType.IP)
    @GetMapping("/download/ip")
    public void downloadByIp(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpServletResponse limitedResponse = getLimitedResponse(request, response);
        limitedResponse.setContentType("application/octet-stream");
        limitedResponse.setHeader("Content-Disposition", "attachment; filename=ip-limit-test.bin");
        limitedResponse.setHeader("X-Test-Type", "IP Limit");
        generateTestData(limitedResponse, 5 * 1024 * 1024); // 5MB
    }

    /**
     * 自定义限速测试
     */
    @GetMapping("/download/custom")
    public void downloadCustom(@RequestParam long bandwidth,
                               @RequestParam(defaultValue = "KB") String unit,
                               @RequestParam(defaultValue = "GLOBAL") String type,
                               HttpServletResponse response) throws IOException {
        BandwidthUnit bandwidthUnit = BandwidthUnit.valueOf(unit.toUpperCase());
        LimitType limitType = LimitType.valueOf(type.toUpperCase());

        response.setContentType("application/json");
        response.setHeader("X-Test-Type", "Custom Limit");
        response.setHeader("X-Bandwidth", bandwidth + " " + unit);
        response.setHeader("X-Limit-Type", type);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Custom bandwidth limit request");
        result.put("bandwidth", bandwidth);
        result.put("unit", unit);
        result.put("type", type);
        result.put("note", "This endpoint shows the parameters. Use the annotated endpoints for actual limiting.");

        response.getWriter().write(toJson(result));
    }

    /**
     * 获取限速统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        if (bandwidthLimitInterceptor != null) {
            BandwidthLimitManager.BandwidthLimitStats limitStats = bandwidthLimitManager.getStats();
            stats.put("globalCapacity", BandwidthUnit.formatBytes(limitStats.globalCapacity()));
            stats.put("globalRefillRate", BandwidthUnit.formatBytes(limitStats.globalRefillRate()) + "/s");
            stats.put("globalAvailableTokens", BandwidthUnit.formatBytes(limitStats.globalAvailableTokens()));
            stats.put("globalBytesConsumed", BandwidthUnit.formatBytes(limitStats.globalBytesConsumed()));
            stats.put("globalActualRate", BandwidthUnit.formatBytes((long) limitStats.globalActualRate()) + "/s");
            stats.put("globalUtilization", String.format("%.1f%%", limitStats.globalUtilization() * 100));
            stats.put("apiBucketCount", limitStats.apiBucketCount());
            stats.put("userBucketCount", limitStats.userBucketCount());
            stats.put("ipBucketCount", limitStats.ipBucketCount());
        } else
            stats.put("error", "BandwidthLimitInterceptor not available");

        return stats;
    }

    /**
     * 重置全局限速
     */
    @PostMapping("/reset/global")
    public Map<String, Object> resetGlobal() {
        Map<String, Object> result = new HashMap<>();
        bandwidthLimitManager.resetGlobalBucket();
        result.put("status", "success");
        result.put("message", "Global bandwidth limit bucket reset");

        return result;
    }

    /**
     * 清除所有限速桶
     */
    @PostMapping("/reset/all")
    public Map<String, Object> resetAll() {
        Map<String, Object> result = new HashMap<>();

        bandwidthLimitManager.clearAllBuckets();
        result.put("status", "success");
        result.put("message", "All bandwidth limit buckets cleared");

        return result;
    }

    /**
     * 生成测试数据
     */
    private void generateTestData(HttpServletResponse response, int size) throws IOException {
        response.setContentLengthLong(size);

        byte[] buffer = new byte[8192];
        byte[] pattern = "This is a bandwidth limit test data. ".getBytes(StandardCharsets.UTF_8);

        int patternPos = 0;
        int remaining = size;

        while (remaining > 0) {
            int chunkSize = Math.min(buffer.length, remaining);

            for (int i = 0; i < chunkSize; i++) {
                buffer[i] = pattern[patternPos];
                patternPos = (patternPos + 1) % pattern.length;
            }

            response.getOutputStream().write(buffer, 0, chunkSize);
            remaining -= chunkSize;
        }

        response.getOutputStream().flush();
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first)
                sb.append(",");

            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                sb.append(value);
            } else {
                sb.append("\"").append(value).append("\"");
            }
            first = false;
        }

        sb.append("}");
        return sb.toString();
    }

    private static final String WRAPPED_RESPONSE_ATTR = "BandwidthLimitWrappedResponse";

    /**
     * 获取限速响应包装器（如果存在）
     */
    public static HttpServletResponse getLimitedResponse(HttpServletRequest request, HttpServletResponse defaultResponse) {
        BandwidthLimitResponseWrapper wrappedResponse =
                (BandwidthLimitResponseWrapper) request.getAttribute(WRAPPED_RESPONSE_ATTR);

        if (wrappedResponse != null)
            return wrappedResponse;

        return defaultResponse;
    }

    /**
     * 检查是否应用了限速
     */
    public static boolean isLimited(HttpServletRequest request) {
        return request.getAttribute(WRAPPED_RESPONSE_ATTR) != null;
    }
}