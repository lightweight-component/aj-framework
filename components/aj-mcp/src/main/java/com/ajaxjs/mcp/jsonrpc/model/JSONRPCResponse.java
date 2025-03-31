package com.ajaxjs.mcp.jsonrpc.model;

import com.ajaxjs.mcp.jsonrpc.schema.JSONRPCMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 表示一个 JSON-RPC 响应。
 */
@Data
public class JSONRPCResponse implements JSONRPCMessage {
    private String jsonrpc;

    private Object id;

    private Object result;

    private JSONRPCError error;
}
