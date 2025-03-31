package com.ajaxjs.mcp.jsonrpc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Existing Enums and Base Types (from previous implementation)
 */
public enum Role {
    @JsonProperty("user") USER,
    @JsonProperty("assistant") ASSISTANT
}
