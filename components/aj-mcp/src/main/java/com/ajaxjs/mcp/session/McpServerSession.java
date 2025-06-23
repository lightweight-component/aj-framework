package com.ajaxjs.mcp.session;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class McpServerSession implements McpSession {
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
