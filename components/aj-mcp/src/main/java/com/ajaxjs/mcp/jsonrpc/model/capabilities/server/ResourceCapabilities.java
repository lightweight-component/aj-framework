package com.ajaxjs.mcp.jsonrpc.model.capabilities.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResourceCapabilities {
    Boolean subscribe;

    Boolean listChanged;
}
