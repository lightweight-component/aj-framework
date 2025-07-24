package com.ajaxjs.business.web.rate_limiter.ratelimiter2;

/**
 * 限流算法: 固定时间窗口、滑动窗口、令牌桶等
 */
public interface RateLimitAlg {
    boolean tryAcquire() throws InterruptedException;
}