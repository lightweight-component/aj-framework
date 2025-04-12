/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.ajaxjs.mcp.client;

import com.ajaxjs.mcp.client.mcpclient.McpClient;
import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.Root;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.ClientCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.server.ServerCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.progress.LoggingLevel;
import com.ajaxjs.mcp.jsonrpc.model.prompt.GetPromptRequest;
import com.ajaxjs.mcp.jsonrpc.model.prompt.GetPromptResult;
import com.ajaxjs.mcp.jsonrpc.model.prompt.ListPromptsResult;
import com.ajaxjs.mcp.jsonrpc.model.request.InitializeResult;
import com.ajaxjs.mcp.jsonrpc.model.resources.*;
import com.ajaxjs.mcp.jsonrpc.model.tool.CallToolRequest;
import com.ajaxjs.mcp.jsonrpc.model.tool.CallToolResult;
import com.ajaxjs.mcp.jsonrpc.model.tool.ListToolsResult;
import lombok.extern.slf4j.Slf4j;

/**
 * A synchronous client implementation for the Model Context Protocol (MCP) that wraps an
 * {@link McpAsyncClient} to provide blocking operations.
 *
 * <p>
 * This client implements the MCP specification by delegating to an asynchronous client
 * and blocking on the results. Key features include:
 * <ul>
 * <li>Synchronous, blocking API for simpler integration in non-reactive applications
 * <li>Tool discovery and invocation for server-provided functionality
 * <li>Resource access and management with URI-based addressing
 * <li>Prompt template handling for standardized AI interactions
 * <li>Real-time notifications for tools, resources, and prompts changes
 * <li>Structured logging with configurable severity levels
 * </ul>
 *
 * <p>
 * The client follows the same lifecycle as its async counterpart:
 * <ol>
 * <li>Initialization - Establishes connection and negotiates capabilities
 * <li>Normal Operation - Handles requests and notifications
 * <li>Graceful Shutdown - Ensures clean connection termination
 * </ol>
 *
 * <p>
 * This implementation implements {@link AutoCloseable} for resource cleanup and provides
 * both immediate and graceful shutdown options. All operations block until completion or
 * timeout, making it suitable for traditional synchronous programming models.
 *
 * @author Dariusz Jędrzejczyk
 * @author Christian Tzolov
 * @see McpClient
 * @see McpAsyncClient
 */
@Slf4j
public class McpSyncClient implements AutoCloseable {

    // TODO: Consider providing a client config to set this properly
    // this is currently a concern only because AutoCloseable is used - perhaps it
    // is not a requirement?
    private static final long DEFAULT_CLOSE_TIMEOUT_MS = 10_000L;

    private final McpAsyncClient delegate;

    /**
     * Create a new McpSyncClient with the given delegate.
     *
     * @param delegate the asynchronous kernel on top of which this synchronous client provides a blocking API.
     */
    public McpSyncClient(McpAsyncClient delegate) {
        this.delegate = delegate;
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
     * Get the client capabilities that define the supported features and functionality.
     *
     * @return The client capabilities
     */
    public ClientCapabilities getClientCapabilities() {
        return delegate.getClientCapabilities();
    }

    /**
     * Get the client implementation information.
     *
     * @return The client implementation details
     */
    public Implementation getClientInfo() {
        return delegate.getClientInfo();
    }

    @Override
    public void close() {
        this.delegate.close();
    }

    public boolean closeGracefully() {
        try {
//            this.delegate.closeGracefully().join(Duration.ofMillis(DEFAULT_CLOSE_TIMEOUT_MS));
            this.delegate.closeGracefully().join();
        } catch (RuntimeException e) {
            log.warn("Client didn't close within timeout of {} ms.", DEFAULT_CLOSE_TIMEOUT_MS, e);
            return false;
        }
        return true;
    }

    /**
     * The initialization phase MUST be the first interaction between client and server.
     * During this phase, the client and server:
     * <ul>
     * <li>Establish protocol version compatibility</li>
     * <li>Exchange and negotiate capabilities</li>
     * <li>Share implementation details</li>
     * </ul>
     * <br/>
     * The client MUST initiate this phase by sending an initialize request containing:
     * <ul>
     * <li>The protocol version the client supports</li>
     * <li>The client's capabilities</li>
     * <li>Client implementation information</li>
     * </ul>
     * <p>
     * The server MUST respond with its own capabilities and information:
     * {@link ServerCapabilities}. <br/>
     * After successful initialization, the client MUST send an initialized notification
     * to indicate it is ready to begin normal operations.
     * <p>
     * <br/>
     * <p>
     * <a href=
     * "https://github.com/modelcontextprotocol/specification/blob/main/docs/specification/basic/lifecycle.md#initialization">Initialization
     * Spec</a>
     *
     * @return the initialize result.
     */
    public InitializeResult initialize() {
        // TODO: block takes no argument here as we assume the async client is
        // configured with a requestTimeout at all times
        return this.delegate.initialize().join();
    }

    /**
     * Send a roots/list_changed notification.
     */
    public void rootsListChangedNotification() {
        delegate.rootsListChangedNotification().join();
    }

    /**
     * Add a roots dynamically.
     */
    public void addRoot(Root root) {
        delegate.addRoot(root).join();
    }

    /**
     * Remove a root dynamically.
     */
    public void removeRoot(String rootUri) {
        this.delegate.removeRoot(rootUri).join();
    }

    /**
     * Send a synchronous ping request.
     *
     * @return
     */
    public Object ping() {
        return this.delegate.ping().join();
    }

    // --------------------------
    // Tools
    // --------------------------

    /**
     * Calls a tool provided by the server. Tools enable servers to expose executable
     * functionality that can interact with external systems, perform computations, and
     * take actions in the real world.
     *
     * @param callToolRequest The request containing: - name: The name of the tool to call
     *                        (must match a tool name from tools/list) - arguments: Arguments that conform to the
     *                        tool's input schema
     * @return The tool execution result containing: - content: List of content items
     * (text, images, or embedded resources) representing the tool's output - isError:
     * Boolean indicating if the execution failed (true) or succeeded (false/absent)
     */
    public CallToolResult callTool(CallToolRequest callToolRequest) {
        return delegate.callTool(callToolRequest).join();
    }

    /**
     * Retrieves the list of all tools provided by the server.
     *
     * @return The list of tools result containing: - tools: List of available tools, each
     * with a name, description, and input schema - nextCursor: Optional cursor for
     * pagination if more tools are available
     */
    public ListToolsResult listTools() {
        return this.delegate.listTools().join();
    }

    /**
     * Retrieves a paginated list of tools provided by the server.
     *
     * @param cursor Optional pagination cursor from a previous list request
     * @return The list of tools result containing: - tools: List of available tools, each
     * with a name, description, and input schema - nextCursor: Optional cursor for
     * pagination if more tools are available
     */
    public ListToolsResult listTools(String cursor) {
        return this.delegate.listTools(cursor).join();
    }

    // --------------------------
    // Resources
    // --------------------------

    /**
     * Send a resources/list request.
     *
     * @param cursor the cursor
     * @return the list of resources result.
     */
    public ListResourcesResult listResources(String cursor) {
        return this.delegate.listResources(cursor).join();
    }

    /**
     * Send a resources/list request.
     *
     * @return the list of resources result.
     */
    public ListResourcesResult listResources() {
        return this.delegate.listResources().join();
    }

    /**
     * Send a resources/read request.
     *
     * @param resource the resource to read
     * @return the resource content.
     */
    public ReadResourceResult readResource(Resource resource) {
        return this.delegate.readResource(resource).join();
    }

    /**
     * Send a resources/read request.
     *
     * @param readResourceRequest the read resource request.
     * @return the resource content.
     */
    public ReadResourceResult readResource(ReadResourceRequest readResourceRequest) {
        return this.delegate.readResource(readResourceRequest).join();
    }

    /**
     * Resource templates allow servers to expose parameterized resources using URI
     * templates. Arguments may be auto-completed through the completion API.
     * <p>
     * Request a list of resource templates the server has.
     *
     * @param cursor the cursor
     * @return the list of resource templates result.
     */
    public ListResourceTemplatesResult listResourceTemplates(String cursor) {
        return this.delegate.listResourceTemplates(cursor).join();
    }

    /**
     * Request a list of resource templates the server has.
     *
     * @return the list of resource templates result.
     */
    public ListResourceTemplatesResult listResourceTemplates() {
        return this.delegate.listResourceTemplates().join();
    }

    /**
     * Subscriptions. The protocol supports optional subscriptions to resource changes.
     * Clients can subscribe to specific resources and receive notifications when they
     * change.
     * <p>
     * Send a resources/subscribe request.
     *
     * @param subscribeRequest the subscribe request contains the uri of the resource to  subscribe to.
     */
    public void subscribeResource(SubscribeRequest subscribeRequest) {
        this.delegate.subscribeResource(subscribeRequest).join();
    }

    /**
     * Send a resources/unsubscribe request.
     *
     * @param unsubscribeRequest the unsubscribe request contains the uri of the resource to unsubscribe from.
     */
    public void unsubscribeResource(UnsubscribeRequest unsubscribeRequest) {
        this.delegate.unsubscribeResource(unsubscribeRequest).join();
    }

    // --------------------------
    // Prompts
    // --------------------------
    public ListPromptsResult listPrompts(String cursor) {
        return delegate.listPrompts(cursor).join();
    }

    public ListPromptsResult listPrompts() {
        return this.delegate.listPrompts().join();
    }

    public GetPromptResult getPrompt(GetPromptRequest getPromptRequest) {
        return this.delegate.getPrompt(getPromptRequest).join();
    }

    /**
     * Client can set the minimum logging level it wants to receive from the server.
     *
     * @param loggingLevel the min logging level
     */
    public void setLoggingLevel(LoggingLevel loggingLevel) {
        this.delegate.setLoggingLevel(loggingLevel).join();
    }

}
