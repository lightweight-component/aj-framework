/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.ajaxjs.mcp.server.asyncserver;

import com.ajaxjs.mcp.McpConstant;
import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.server.ServerCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.progress.LoggingMessageNotification;
import com.ajaxjs.mcp.server.McpServer;
import com.ajaxjs.mcp.server.features.AsyncPromptSpecification;
import com.ajaxjs.mcp.server.features.AsyncResourceSpecification;
import com.ajaxjs.mcp.server.features.AsyncToolSpecification;
import com.ajaxjs.mcp.server.features.ServerFeaturesAsync;
import com.ajaxjs.mcp.session.McpClientSession;
import com.ajaxjs.mcp.transport.McpServerTransportProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The Model Context Protocol (MCP) server implementation that provides asynchronous
 * communication using Project Reactor's CompletableFuture  and Flux types.
 *
 * <p>
 * This server implements the MCP specification, enabling AI models to expose tools,
 * resources, and prompts through a standardized interface. Key features include:
 * <ul>
 * <li>Asynchronous communication using reactive programming patterns
 * <li>Dynamic tool registration and management
 * <li>Resource handling with URI-based addressing
 * <li>Prompt template management
 * <li>Real-time client notifications for state changes
 * <li>Structured logging with configurable severity levels
 * <li>Support for client-side AI model sampling
 * </ul>
 *
 * <p>
 * The server follows a lifecycle:
 * <ol>
 * <li>Initialization - Accepts client connections and negotiates capabilities
 * <li>Normal Operation - Handles client requests and sends notifications
 * <li>Graceful Shutdown - Ensures clean connection termination
 * </ol>
 *
 * <p>
 * This implementation uses Project Reactor for non-blocking operations, making it
 * suitable for high-throughput scenarios and reactive applications. All operations return
 * CompletableFuture  or Flux types that can be composed into reactive pipelines.
 *
 * <p>
 * The server supports runtime modification of its capabilities through methods like
 * {@link #addTool}, {@link #addResource}, and {@link #addPrompt}, automatically notifying
 * connected clients of changes when configured to do so.
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 * @see McpServer
 * @see McpClientSession
 */
@Slf4j
public class McpAsyncServer implements McpConstant {
    private final McpAsyncServer delegate;

    public McpAsyncServer() {
        this.delegate = null;
    }

    /**
     * Create a new McpAsyncServer with the given transport provider and capabilities.
     *
     * @param mcpTransportProvider The transport layer implementation for MCP
     *                             communication.
     * @param features             The MCP server supported features.
     * @param objectMapper         The ObjectMapper to use for JSON serialization/deserialization
     */
    public McpAsyncServer(McpServerTransportProvider mcpTransportProvider, ObjectMapper objectMapper, ServerFeaturesAsync features) {
        this.delegate = new AsyncServerImpl(mcpTransportProvider, objectMapper, features);
    }

    /**
     * Get the server capabilities that define the supported features and functionality.
     *
     * @return The server capabilities
     */
    public ServerCapabilities getServerCapabilities() {
        return delegate.getServerCapabilities();
    }

    /**
     * Get the server implementation information.
     *
     * @return The server implementation details
     */
    public Implementation getServerInfo() {
        return delegate.getServerInfo();
    }

    /**
     * Gracefully closes the server, allowing any in-progress operations to complete.
     *
     * @return A CompletableFuture  that completes when the server has been closed
     */
    public CompletableFuture<Void> closeGracefully() {
        return delegate.closeGracefully();
    }

    /**
     * Close the server immediately.
     */
    public void close() {
        delegate.close();
    }

    // ---------------------------------------
    // Tool Management
    // ---------------------------------------

    /**
     * Add a new tool specification at runtime.
     *
     * @param toolSpecification The tool specification to add
     * @return CompletableFuture  that completes when clients have been notified of the change
     */
    public CompletableFuture<Void> addTool(AsyncToolSpecification toolSpecification) {
        return delegate.addTool(toolSpecification);
    }

    /**
     * Remove a tool handler at runtime.
     *
     * @param toolName The name of the tool handler to remove
     * @return CompletableFuture  that completes when clients have been notified of the change
     */
    public CompletableFuture<Void> removeTool(String toolName) {
        return delegate.removeTool(toolName);
    }

    /**
     * Notifies clients that the list of available tools has changed.
     *
     * @return A CompletableFuture  that completes when all clients have been notified
     */
    public CompletableFuture<Void> notifyToolsListChanged() {
        return delegate.notifyToolsListChanged();
    }

    // ---------------------------------------
    // Resource Management
    // ---------------------------------------

    /**
     * Add a new resource handler at runtime.
     *
     * @param resourceHandler The resource handler to add
     * @return CompletableFuture  that completes when clients have been notified of the change
     */
    public CompletableFuture<Void> addResource(AsyncResourceSpecification resourceHandler) {
        return delegate.addResource(resourceHandler);
    }

    /**
     * Remove a resource handler at runtime.
     *
     * @param resourceUri The URI of the resource handler to remove
     * @return CompletableFuture  that completes when clients have been notified of the change
     */
    public CompletableFuture<Void> removeResource(String resourceUri) {
        return delegate.removeResource(resourceUri);
    }

    /**
     * Notifies clients that the list of available resources has changed.
     *
     * @return A CompletableFuture  that completes when all clients have been notified
     */
    public CompletableFuture<Void> notifyResourcesListChanged() {
        return this.delegate.notifyResourcesListChanged();
    }

    // ---------------------------------------
    // Prompt Management
    // ---------------------------------------

    /**
     * Add a new prompt handler at runtime.
     *
     * @param promptSpecification The prompt handler to add
     * @return CompletableFuture  that completes when clients have been notified of the change
     */
    public CompletableFuture<Void> addPrompt(AsyncPromptSpecification promptSpecification) {
        return this.delegate.addPrompt(promptSpecification);
    }

    /**
     * Remove a prompt handler at runtime.
     *
     * @param promptName The name of the prompt handler to remove
     * @return CompletableFuture  that completes when clients have been notified of the change
     */
    public CompletableFuture<Void> removePrompt(String promptName) {
        return this.delegate.removePrompt(promptName);
    }

    /**
     * Notifies clients that the list of available prompts has changed.
     *
     * @return A CompletableFuture  that completes when all clients have been notified
     */
    public CompletableFuture<Void> notifyPromptsListChanged() {
        return this.delegate.notifyPromptsListChanged();
    }

    // ---------------------------------------
    // Logging Management
    // ---------------------------------------

    /**
     * Send a logging message notification to all connected clients. Messages below the
     * current minimum logging level will be filtered out.
     *
     * @param loggingMessageNotification The logging message to send
     * @return A CompletableFuture  that completes when the notification has been sent
     */
    public CompletableFuture<Void> loggingNotification(LoggingMessageNotification loggingMessageNotification) {
        return this.delegate.loggingNotification(loggingMessageNotification);
    }

    // ---------------------------------------
    // Sampling
    // ---------------------------------------

    /**
     * This method is package-private and used for test only. Should not be called by user
     * code.
     *
     * @param protocolVersions the Client supported protocol versions.
     */
    void setProtocolVersions(List<String> protocolVersions) {
        this.delegate.setProtocolVersions(protocolVersions);
    }


}
