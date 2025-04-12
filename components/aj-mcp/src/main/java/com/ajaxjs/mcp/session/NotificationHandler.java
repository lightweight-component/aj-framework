package com.ajaxjs.mcp.session;

import com.ajaxjs.mcp.server.McpAsyncServerExchange;

import java.util.concurrent.CompletableFuture;

/**
 * A handler for client-initiated notifications.
 */
public interface NotificationHandler {
    /**
     * Handles a notification from the client.
     *
     * @param exchange the exchange associated with the client that allows calling back to the connected client or inspecting its capabilities.
     * @param params   the parameters of the notification.
     * @return a Mono that completes once the notification is handled.
     */
    CompletableFuture<Void> handle(McpAsyncServerExchange exchange, Object params);
}