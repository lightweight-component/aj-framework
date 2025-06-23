/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.ajaxjs.mcp.client.mcpclient;

import com.ajaxjs.mcp.client.McpAsyncClient;
import com.ajaxjs.mcp.client.McpSyncClient;
import com.ajaxjs.mcp.transport.McpClientTransport;
import com.ajaxjs.mcp.transport.McpTransport;

/**
 * Factory class for creating Model Context Protocol (MCP) clients. MCP is a protocol that
 * enables AI models to interact with external tools and resources through a standardized
 * interface.
 *
 * <p>
 * This class serves as the main entry point for establishing connections with MCP
 * servers, implementing the client-side of the MCP specification. The protocol follows a
 * client-server architecture where:
 * <ul>
 * <li>The client (this implementation) initiates connections and sends requests
 * <li>The server responds to requests and provides access to tools and resources
 * <li>Communication occurs through a transport layer (e.g., stdio, SSE) using JSON-RPC
 * 2.0
 * </ul>
 *
 * <p>
 * The class provides factory methods to create either:
 * <ul>
 * <li>{@link McpAsyncClient} for non-blocking operations with CompletableFuture responses
 * <li>{@link McpSyncClient} for blocking operations with direct responses
 * </ul>
 *
 * <p>
 * Example of creating a basic synchronous client: <pre>{@code
 * McpClient.sync(transport)
 *     .requestTimeout(Duration.ofSeconds(5))
 *     .build();
 * }</pre>
 * <p>
 * Example of creating a basic asynchronous client: <pre>{@code
 * McpClient.async(transport)
 *     .requestTimeout(Duration.ofSeconds(5))
 *     .build();
 * }</pre>
 *
 * <p>
 * Example with advanced asynchronous configuration: <pre>{@code
 * McpClient.async(transport)
 *     .requestTimeout(Duration.ofSeconds(10))
 *     .capabilities(new ClientCapabilities(...))
 *     .clientInfo(new Implementation("My Client", "1.0.0"))
 *     .roots(new Root("file://workspace", "Workspace Files"))
 *     .toolsChangeConsumer(tools -> Mono.fromRunnable(() -> System.out.println("Tools updated: " + tools)))
 *     .resourcesChangeConsumer(resources -> Mono.fromRunnable(() -> System.out.println("Resources updated: " + resources)))
 *     .promptsChangeConsumer(prompts -> Mono.fromRunnable(() -> System.out.println("Prompts updated: " + prompts)))
 *     .loggingConsumer(message -> Mono.fromRunnable(() -> System.out.println("Log message: " + message)))
 *     .build();
 * }</pre>
 *
 * <p>
 * The client supports:
 * <ul>
 * <li>Tool discovery and invocation
 * <li>Resource access and management
 * <li>Prompt template handling
 * <li>Real-time updates through change consumers
 * <li>Custom sampling strategies
 * <li>Structured logging with severity levels
 * </ul>
 *
 * <p>
 * The client supports structured logging through the MCP logging utility:
 * <ul>
 * <li>Eight severity levels from DEBUG to EMERGENCY
 * <li>Optional logger name categorization
 * <li>Configurable logging consumers
 * <li>Server-controlled minimum log level
 * </ul>
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 * @see McpAsyncClient
 * @see McpSyncClient
 * @see McpTransport
 */
public interface McpClient {
    /**
     * Start building a synchronous MCP client with the specified transport layer. The
     * synchronous MCP client provides blocking operations. Synchronous clients wait for
     * each operation to complete before returning, making them simpler to use but
     * potentially less performant for concurrent operations. The transport layer handles
     * the low-level communication between client and server using protocols like stdio or
     * Server-Sent Events (SSE).
     *
     * @param transport The transport layer implementation for MCP communication. Common
     *                  implementations include {@code StdioClientTransport} for stdio-based communication
     *                  and {@code SseClientTransport} for SSE-based communication.
     * @return A new builder instance for configuring the client
     * @throws IllegalArgumentException if transport is null
     */
    static SyncSpec sync(McpClientTransport transport) {
        return new SyncSpec(transport);
    }

    /**
     * Start building an asynchronous MCP client with the specified transport layer. The
     * asynchronous MCP client provides non-blocking operations. Asynchronous clients
     * return reactive primitives (Mono/Flux) immediately, allowing for concurrent
     * operations and reactive programming patterns. The transport layer handles the
     * low-level communication between client and server using protocols like stdio or
     * Server-Sent Events (SSE).
     *
     * @param transport The transport layer implementation for MCP communication. Common
     *                  implementations include {@code StdioClientTransport} for stdio-based communication
     *                  and {@code SseClientTransport} for SSE-based communication.
     * @return A new builder instance for configuring the client
     * @throws IllegalArgumentException if transport is null
     */
    static AsyncSpec async(McpClientTransport transport) {
        return new AsyncSpec(transport);
    }
}
