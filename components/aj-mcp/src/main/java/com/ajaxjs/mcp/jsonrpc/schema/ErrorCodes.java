package com.ajaxjs.mcp.jsonrpc.schema;

/**
 * Standard error codes used in MCP JSON-RPC responses.
 */
public interface ErrorCodes {
    /**
     * Invalid JSON was received by the server.
     */
    int PARSE_ERROR = -32700;

    /**
     * The JSON sent is not a valid Request object.
     */
    int INVALID_REQUEST = -32600;

    /**
     * The method does not exist / is not available.
     */
    int METHOD_NOT_FOUND = -32601;

    /**
     * Invalid method parameter(s).
     */
    int INVALID_PARAMS = -32602;

    /**
     * Internal JSON-RPC error.
     */
    int INTERNAL_ERROR = -32603;
}
