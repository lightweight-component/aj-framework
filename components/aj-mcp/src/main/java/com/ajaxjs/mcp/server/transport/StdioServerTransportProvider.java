/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.ajaxjs.mcp.server.transport;

import com.ajaxjs.mcp.session.Factory;
import com.ajaxjs.mcp.session.McpServerSession;
import com.ajaxjs.mcp.transport.McpServerTransportProvider;
import com.ajaxjs.mcp.utils.McpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of the MCP Stdio transport provider for servers that communicates using
 * standard input/output streams. Messages are exchanged as newline-delimited JSON-RPC
 * messages over stdin/stdout, with errors and debug information sent to stderr.
 *
 * @author Christian Tzolov
 */
@Slf4j
public class StdioServerTransportProvider implements McpServerTransportProvider {

    private final ObjectMapper objectMapper;

    private final InputStream inputStream;

    private final OutputStream outputStream;

    private McpServerSession session;

    private final AtomicBoolean isClosing = new AtomicBoolean(false);

    private final Sinks.One<Void> inboundReady = Sinks.one();

    /**
     * Creates a new StdioServerTransportProvider with a default ObjectMapper and System
     * streams.
     */
    public StdioServerTransportProvider() {
        this(new ObjectMapper());
    }

    /**
     * Creates a new StdioServerTransportProvider with the specified ObjectMapper and
     * System streams.
     *
     * @param objectMapper The ObjectMapper to use for JSON serialization/deserialization
     */
    public StdioServerTransportProvider(ObjectMapper objectMapper) {
        this(objectMapper, System.in, System.out);
    }

    /**
     * Creates a new StdioServerTransportProvider with the specified ObjectMapper and
     * streams.
     *
     * @param objectMapper The ObjectMapper to use for JSON serialization/deserialization
     * @param inputStream  The input stream to read from
     * @param outputStream The output stream to write to
     */
    public StdioServerTransportProvider(ObjectMapper objectMapper, InputStream inputStream, OutputStream outputStream) {
        Objects.requireNonNull(objectMapper, "The ObjectMapper can not be null");
        Objects.requireNonNull(inputStream, "The InputStream can not be null");
        Objects.requireNonNull(outputStream, "The OutputStream can not be null");

        this.objectMapper = objectMapper;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void setSessionFactory(Factory sessionFactory) {
        // Create a single session for the stdio connection
        StdioMcpSessionTransport transport = new StdioMcpSessionTransport();
        this.session = sessionFactory.create(transport);
        transport.initProcessing();
    }

    @Override
    public CompletableFuture<Void> notifyClients(String method, Map<String, Object> params) {
        if (this.session == null)
            return McpUtils.error("No session to close");

        return session.sendNotification(method, params).doOnError(e -> log.error("Failed to send notification: {}", e.getMessage()));
    }

    @Override
    public CompletableFuture<Void> closeGracefully() {
        if (session == null)
            return CompletableFuture.completedFuture(null);

        return session.closeGracefully();
    }
}
