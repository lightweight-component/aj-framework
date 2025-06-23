package com.ajaxjs.mcp.session;

import com.ajaxjs.mcp.server.McpAsyncServerExchange;

import java.util.concurrent.CompletableFuture;

/**
 * A handler for client-initiated requests.
 *
 * @param <T> the type of the response that is expected as a result of handling the request.
 */
public interface RequestHandler<T> {
    /**
     * Handles a request from the client.
     *
     * @param exchange the exchange associated with the client that allows calling back to the connected client or inspecting its capabilities.
     * @param params   the parameters of the request.
     * @return a Mono that will emit the response to the request.
     */
    CompletableFuture<T> handle(McpAsyncServerExchange exchange, Object params);
}