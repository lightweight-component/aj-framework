package com.ajaxjs.business.web.rate_limiter.ratelimiter1;

/**
 * TimestampHolder
 *
 * @author Tian ZhongBo
 */
public class TimestampHolder {
    private long timestamp;

    public TimestampHolder() {
        this(System.currentTimeMillis());
    }

    public TimestampHolder(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
