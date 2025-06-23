/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.ajaxjs.mcp.server;

import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.server.ServerCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.progress.LoggingMessageNotification;
import com.ajaxjs.mcp.server.asyncserver.McpAsyncServer;
import com.ajaxjs.mcp.server.features.*;

import java.util.Objects;

/**
 * A synchronous implementation of the Model Context Protocol (MCP) server that wraps
 * {@link McpAsyncServer} to provide blocking operations. This class delegates all
 * operations to an underlying async server instance while providing a simpler,
 * synchronous API for scenarios where reactive programming is not required.
 *
 * <p>
 * The MCP server enables AI models to expose tools, resources, and prompts through a
 * standardized interface. Key features available through this synchronous API include:
 * <ul>
 * <li>Tool registration and management for extending AI model capabilities
 * <li>Resource handling with URI-based addressing for providing context
 * <li>Prompt template management for standardized interactions
 * <li>Real-time client notifications for state changes
 * <li>Structured logging with configurable severity levels
 * <li>Support for client-side AI model sampling
 * </ul>
 *
 * <p>
 * While {@link McpAsyncServer} uses Project Reactor's Mono and Flux types for
 * non-blocking operations, this class converts those into blocking calls, making it more
 * suitable for:
 * <ul>
 * <li>Traditional synchronous applications
 * <li>Simple scripting scenarios
 * <li>Testing and debugging
 * <li>Cases where reactive programming adds unnecessary complexity
 * </ul>
 *
 * <p>
 * The server supports runtime modification of its capabilities through methods like
 * {@link #addTool}, {@link #addResource}, and {@link #addPrompt}, automatically notifying
 * connected clients of changes when configured to do so.
 *
 * @see McpAsyncServer
 */
public class McpSyncServer {
    /**
     * The async server to wrap.
     */
    private final McpAsyncServer asyncServer;

    /**
     * Creates a new synchronous server that wraps the provided async server.
     *
     * @param asyncServer The async server to wrap
     */
    public McpSyncServer(McpAsyncServer asyncServer) {
        Objects.requireNonNull(asyncServer, "Async server must not be null");
        this.asyncServer = asyncServer;
    }

    /**
     * Add a new tool handler.
     *
     * @param toolHandler The tool handler to add
     */
    public void addTool(SyncToolSpecification toolHandler) {
        this.asyncServer.addTool(AsyncToolSpecification.fromSync(toolHandler)).join();
    }

    /**
     * Remove a tool handler.
     *
     * @param toolName The name of the tool handler to remove
     */
    public void removeTool(String toolName) {
        this.asyncServer.removeTool(toolName).join();
    }

    /**
     * Add a new resource handler.
     *
     * @param resourceHandler The resource handler to add
     */
    public void addResource(SyncResourceSpecification resourceHandler) {
        this.asyncServer.addResource(AsyncResourceSpecification.fromSync(resourceHandler)).join();
    }

    /**
     * Remove a resource handler.
     *
     * @param resourceUri The URI of the resource handler to remove
     */
    public void removeResource(String resourceUri) {
        this.asyncServer.removeResource(resourceUri).join();
    }

    /**
     * Add a new prompt handler.
     *
     * @param promptSpecification The prompt specification to add
     */
    public void addPrompt(SyncPromptSpecification promptSpecification) {
        asyncServer.addPrompt(AsyncPromptSpecification.fromSync(promptSpecification)).join();
    }

    /**
     * Remove a prompt handler.
     *
     * @param promptName The name of the prompt handler to remove
     */
    public void removePrompt(String promptName) {
        asyncServer.removePrompt(promptName).join();
    }

    /**
     * Notify clients that the list of available tools has changed.
     */
    public void notifyToolsListChanged() {
        asyncServer.notifyToolsListChanged().join();
    }

    /**
     * Get the server capabilities that define the supported features and functionality.
     *
     * @return The server capabilities
     */
    public ServerCapabilities getServerCapabilities() {
        return asyncServer.getServerCapabilities();
    }

    /**
     * Get the server implementation information.
     *
     * @return The server implementation details
     */
    public Implementation getServerInfo() {
        return asyncServer.getServerInfo();
    }

    /**
     * Notify clients that the list of available resources has changed.
     */
    public void notifyResourcesListChanged() {
        asyncServer.notifyResourcesListChanged().join();
    }

    /**
     * Notify clients that the list of available prompts has changed.
     */
    public void notifyPromptsListChanged() {
        asyncServer.notifyPromptsListChanged().join();
    }

    /**
     * Send a logging message notification to all clients.
     *
     * @param loggingMessageNotification The logging message notification to send
     */
    public void loggingNotification(LoggingMessageNotification loggingMessageNotification) {
        asyncServer.loggingNotification(loggingMessageNotification).join();
    }

    /**
     * Close the server gracefully.
     */
    public void closeGracefully() {
        asyncServer.closeGracefully().join();
    }

    /**
     * Close the server immediately.
     */
    public void close() {
        this.asyncServer.close();
    }

    /**
     * Get the underlying async server instance.
     *
     * @return The wrapped async server
     */
    public McpAsyncServer getAsyncServer() {
        return this.asyncServer;
    }

}
