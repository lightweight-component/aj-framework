package com.ajaxjs.framework.shamir.model;

import lombok.Data;

@Data
public class SessionMetadata {
    private String sessionId;

    private int totalShares;

    private int threshold;

    private long createTime;

    public SessionMetadata(String sessionId, int totalShares, int threshold) {
        this.sessionId = sessionId;
        this.totalShares = totalShares;
        this.threshold = threshold;
        this.createTime = System.currentTimeMillis();
    }
}
