package com.ajaxjs.mcp.transport;

import com.ajaxjs.mcp.jsonrpc.schema.JSONRPCMessage;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Marker interface for the client-side MCP transport.
 */
public interface McpClientTransport extends McpTransport {
    CompletableFuture<Void> connect(Function<CompletableFuture<JSONRPCMessage>, CompletableFuture<JSONRPCMessage>> handler);
}
