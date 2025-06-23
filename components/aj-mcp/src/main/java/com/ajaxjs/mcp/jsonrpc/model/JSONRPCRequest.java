package com.ajaxjs.mcp.jsonrpc.model;

import com.ajaxjs.mcp.jsonrpc.schema.JSONRPCMessage;
import lombok.Data;

/**
 * 表示一个 JSON-RPC 请求。
 */
@Data
public class JSONRPCRequest implements JSONRPCMessage {
    private String jsonrpc;

    private String method;

    private Object id;

    private Object params;
}
