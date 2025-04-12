package com.ajaxjs.mcp.session;

import com.ajaxjs.mcp.jsonrpc.model.JSONRPCResponse;
import com.ajaxjs.mcp.transport.McpClientTransport;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@AllArgsConstructor
public class McpClientSession implements McpSession {
    /**
     * Duration to wait for request responses before timing out
     */
    private final Duration requestTimeout;

    /**
     * Transport layer implementation for message exchange
     */
    private final McpClientTransport transport;

    /**
     * Map of pending responses keyed by request ID
     */
    private final ConcurrentHashMap<Object, CompletableFuture<JSONRPCResponse>> pendingResponses = new ConcurrentHashMap<>();

    /**
     * Map of request handlers keyed by method name
     */
    private final ConcurrentHashMap<String, RequestHandler<?>> requestHandlers = new ConcurrentHashMap<>();

    /**
     * Map of notification handlers keyed by method name
     */
    private final ConcurrentHashMap<String, NotificationHandler> notificationHandlers = new ConcurrentHashMap<>();

    /**
     * Session-specific prefix for request IDs
     */
    private final String sessionPrefix = UUID.randomUUID().toString().substring(0, 8);

    /**
     * Atomic counter for generating unique request IDs
     */
    private final AtomicLong requestCounter = new AtomicLong(0);

    private final Disposable connection;

    @Override
    public <T> CompletableFuture<T> sendRequest(String method, Object requestParams, TypeReference<T> typeRef) {
        return null;
    }

    @Override
    public CompletableFuture<Void> sendNotification(String method, Map<String, Object> params) {
        return null;
    }

    @Override
    public CompletableFuture<Void> closeGracefully() {
        return null;
    }

    @Override
    public void close() {

    }
}
