package com.ajaxjs.mcp.jsonrpc.model.sampling;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum StopReason {
    @JsonProperty("endTurn") END_TURN,

    @JsonProperty("stopSequence") STOP_SEQUENCE,

    @JsonProperty("maxTokens") MAX_TOKENS
}
