package com.ajaxjs.mcp.jsonrpc.model.request;

import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.ClientCapabilities;
import com.ajaxjs.mcp.jsonrpc.schema.Request;
import lombok.Data;

@Data
public class InitializeRequest implements Request {
    String protocolVersion;

    ClientCapabilities capabilities;

    Implementation clientInfo;
}
