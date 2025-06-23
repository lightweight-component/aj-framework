package com.ajaxjs.mcp.model;

import com.ajaxjs.mcp.jsonrpc.model.JSONRPCError;

public class McpError extends RuntimeException {
    private JSONRPCError jsonRpcError;

    public McpError(JSONRPCError jsonRpcError) {
        super(jsonRpcError.getMessage());
        this.jsonRpcError = jsonRpcError;
    }

    public McpError(Object error) {
        super(error.toString());
    }

    public JSONRPCError getJsonRpcError() {
        return jsonRpcError;
    }

}
