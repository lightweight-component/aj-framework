package com.ajaxjs.mcp.server.features;

import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.Root;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.server.*;
import com.ajaxjs.mcp.jsonrpc.model.resources.ResourceTemplate;
import com.ajaxjs.mcp.server.McpSyncServerExchange;
import com.ajaxjs.mcp.utils.McpUtils;
import lombok.Data;

import java.util.*;
import java.util.function.BiConsumer;


/**
 * Synchronous server features specification.
 */
@Data
public class ServerFeaturesSync {
    Implementation serverInfo;

    ServerCapabilities serverCapabilities;

    List<SyncToolSpecification> tools;

    Map<String, SyncResourceSpecification> resources;

    List<ResourceTemplate> resourceTemplates;

    Map<String, SyncPromptSpecification> prompts;

    List<BiConsumer<McpSyncServerExchange, List<Root>>> rootsChangeConsumers;

    /**
     * Create an instance and validate the arguments.
     *
     * @param serverInfo           The server implementation details
     * @param serverCapabilities   The server capabilities
     * @param tools                The list of tool specifications
     * @param resources            The map of resource specifications
     * @param resourceTemplates    The list of resource templates
     * @param prompts              The map of prompt specifications
     * @param rootsChangeConsumers The list of consumers that will be notified when the roots list changes
     */
    public ServerFeaturesSync(Implementation serverInfo, ServerCapabilities serverCapabilities,
                              List<SyncToolSpecification> tools,
                              Map<String, SyncResourceSpecification> resources,
                              List<ResourceTemplate> resourceTemplates,
                              Map<String, SyncPromptSpecification> prompts,
                              List<BiConsumer<McpSyncServerExchange, List<Root>>> rootsChangeConsumers) {
        Objects.requireNonNull(serverInfo, "Server info must not be null");

        this.serverInfo = serverInfo;
        this.serverCapabilities = (serverCapabilities != null) ? serverCapabilities
                : new ServerCapabilities(null, // experimental
                new LoggingCapabilities(), // Enable
                // logging
                // by
                // default
                !McpUtils.isEmpty(prompts) ? new PromptCapabilities(false) : null,
                !McpUtils.isEmpty(resources)
                        ? new ResourceCapabilities(false, false) : null,
                !McpUtils.isEmpty(tools) ? new ToolCapabilities(false) : null);

        this.tools = (tools != null) ? tools : new ArrayList<>();
        this.resources = (resources != null) ? resources : new HashMap<>();
        this.resourceTemplates = (resourceTemplates != null) ? resourceTemplates : new ArrayList<>();
        this.prompts = (prompts != null) ? prompts : new HashMap<>();
        this.rootsChangeConsumers = (rootsChangeConsumers != null) ? rootsChangeConsumers : new ArrayList<>();
    }
}
