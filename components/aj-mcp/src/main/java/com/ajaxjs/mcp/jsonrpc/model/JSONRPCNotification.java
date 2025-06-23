package com.ajaxjs.mcp.jsonrpc.model;

import com.ajaxjs.mcp.jsonrpc.schema.JSONRPCMessage;
import lombok.Data;

/**
 * 表示一个 JSON-RPC 通知。
 */
@Data
public class JSONRPCNotification implements JSONRPCMessage {
    private String jsonrpc;

    private String method;

    private Object params;
}
