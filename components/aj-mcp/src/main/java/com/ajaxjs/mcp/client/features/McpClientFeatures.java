/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.ajaxjs.mcp.client.features;

import com.ajaxjs.mcp.client.mcpclient.McpClient;
import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.ClientCapabilities;

/**
 * Representation of features and capabilities for Model Context Protocol (MCP) clients.
 * This class provides two record types for managing client features:
 * <ul>
 * <li>{@link ClientAsync} for non-blocking operations with Project Reactor's Mono responses
 * <li>{@link ClientSync} for blocking operations with direct responses
 * </ul>
 *
 * <p>
 * Each feature specification includes:
 * <ul>
 * <li>Client implementation information and capabilities
 * <li>Root URI mappings for resource access
 * <li>Change notification handlers for tools, resources, and prompts
 * <li>Logging message consumers
 * <li>Message sampling handlers for request processing
 * </ul>
 *
 * <p>
 * The class supports conversion between synchronous and asynchronous specifications
 * through the {@link ClientAsync#fromSync} method, which ensures proper handling of blocking
 * operations in non-blocking contexts by scheduling them on a bounded elastic scheduler.
 *
 * @see McpClient
 * @see Implementation
 * @see ClientCapabilities
 */
public class McpClientFeatures {
}
