package com.ajaxjs.mcp.jsonrpc.model;

import com.ajaxjs.mcp.jsonrpc.schema.JSONRPCMessage;
import lombok.Data;

/**
 * 表示一个 JSON-RPC 错误。
 */
@Data
public class JSONRPCError implements JSONRPCMessage {
    private String jsonrpc;

    private Object id;

    private Object data;

    private String message;
}
