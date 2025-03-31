package com.ajaxjs.mcp.jsonrpc.model.capabilities.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResourceCapabilities {
    Boolean subscribe;

    Boolean listChanged;
}
