package com.ajaxjs.mcp.jsonrpc.model.tool;

import com.ajaxjs.mcp.jsonrpc.schema.Request;
import lombok.Data;

import java.util.Map;

/**
 * Used by the client to call a tool provided by the server.
 */
@Data
public class CallToolRequest implements Request {
    /**
     * The name of the tool to call. This must match a tool name from tools/list.
     */
    String name;

    /**
     * Arguments to pass to the tool. These must conform to the tool's input schema.
     */
    Map<String, Object> arguments;
}
