package com.ajaxjs.mcp.jsonrpc.model.progress;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum LoggingLevel {
    @JsonProperty("debug") DEBUG(0),
    @JsonProperty("info") INFO(1),
    @JsonProperty("notice") NOTICE(2),
    @JsonProperty("warning") WARNING(3),
    @JsonProperty("error") ERROR(4),
    @JsonProperty("critical") CRITICAL(5),
    @JsonProperty("alert") ALERT(6),
    @JsonProperty("emergency") EMERGENCY(7);

    private final int level;

    LoggingLevel(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

}