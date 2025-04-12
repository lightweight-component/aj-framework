package com.ajaxjs.mcp.jsonrpc.model.sampling;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ContextInclusionStrategy {
    @JsonProperty("none") NONE,
    @JsonProperty("thisServer") THIS_SERVER,
    @JsonProperty("allServers") ALL_SERVERS
}
