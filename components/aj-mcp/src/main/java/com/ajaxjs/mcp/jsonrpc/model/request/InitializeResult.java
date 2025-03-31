package com.ajaxjs.mcp.jsonrpc.model.request;

import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.server.ServerCapabilities;
import lombok.Data;

@Data
public class InitializeResult {
    String protocolVersion;

    ServerCapabilities capabilities;

    Implementation serverInfo;

    String instructions;
}
