package com.ajaxjs.mcp.server.features;

import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.Root;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.server.*;
import com.ajaxjs.mcp.jsonrpc.model.resources.ResourceTemplate;
import com.ajaxjs.mcp.server.McpAsyncServerExchange;
import com.ajaxjs.mcp.server.McpSyncServerExchange;
import com.ajaxjs.mcp.utils.McpUtils;
import lombok.Data;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Asynchronous server features specification.
 */
@Data
public class ServerFeaturesAsync {
    Implementation serverInfo;

    ServerCapabilities serverCapabilities;

    List<AsyncToolSpecification> tools;

    Map<String, AsyncResourceSpecification> resources;

    List<ResourceTemplate> resourceTemplates;

    Map<String, AsyncPromptSpecification> prompts;

    List<BiFunction<McpAsyncServerExchange, List<Root>, CompletableFuture<Void>>> rootsChangeConsumers;

    /**
     * Create an instance and validate the arguments.
     *
     * @param serverInfo           The server implementation details
     * @param serverCapabilities   The server capabilities
     * @param tools                The list of tool specifications
     * @param resources            The map of resource specifications
     * @param resourceTemplates    The list of resource templates
     * @param prompts              The map of prompt specifications
     * @param rootsChangeConsumers The list of consumers that will be notified when
     *                             the roots list changes
     */
    public ServerFeaturesAsync(Implementation serverInfo, ServerCapabilities serverCapabilities,
                               List<AsyncToolSpecification> tools, Map<String, AsyncResourceSpecification> resources,
                               List<ResourceTemplate> resourceTemplates,
                               Map<String, AsyncPromptSpecification> prompts,
                               List<BiFunction<McpAsyncServerExchange, List<Root>, CompletableFuture<Void>>> rootsChangeConsumers) {
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

        this.tools = (tools != null) ? tools : Collections.emptyList();
        this.resources = (resources != null) ? resources : new HashMap<>();
        this.resourceTemplates = (resourceTemplates != null) ? resourceTemplates : Collections.emptyList();
        this.prompts = (prompts != null) ? prompts : new HashMap<>();
        this.rootsChangeConsumers = (rootsChangeConsumers != null) ? rootsChangeConsumers : Collections.emptyList();
    }

    /**
     * Convert a synchronous specification into an asynchronous one and provide
     * blocking code offloading to prevent accidental blocking of the non-blocking
     * transport.
     *
     * @param syncSpec a potentially blocking, synchronous specification.
     * @return a specification which is protected from blocking calls specified by the
     * user.
     */
    public static ServerFeaturesAsync fromSync(ServerFeaturesSync syncSpec) {
        List<AsyncToolSpecification> tools = new ArrayList<>();

        for (SyncToolSpecification tool : syncSpec.getTools())
            tools.add(AsyncToolSpecification.fromSync(tool));

        Map<String, AsyncResourceSpecification> resources = new HashMap<>();
        syncSpec.getResources().forEach((key, resource) -> resources.put(key, AsyncResourceSpecification.fromSync(resource)));

        Map<String, AsyncPromptSpecification> prompts = new HashMap<>();
        syncSpec.getPrompts().forEach((key, prompt) -> prompts.put(key, AsyncPromptSpecification.fromSync(prompt)));

        List<BiFunction<McpAsyncServerExchange, List<Root>, CompletableFuture<Void>>> rootChangeConsumers = new ArrayList<>();

        for (BiConsumer<McpSyncServerExchange, List<Root>> rootChangeConsumer : syncSpec.getRootsChangeConsumers()) {
            rootChangeConsumers.add((exchange, list) -> Mono
                    .<Void>fromRunnable(() -> rootChangeConsumer.accept(new McpSyncServerExchange(exchange), list))
                    .subscribeOn(Schedulers.boundedElastic()));
        }

        return new ServerFeaturesAsync(syncSpec.getServerInfo(), syncSpec.getServerCapabilities(), tools, resources,
                syncSpec.getResourceTemplates(), prompts, rootChangeConsumers);
    }
}
