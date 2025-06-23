package com.ajaxjs.mcp.server.asyncserver;

import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.Root;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.server.ServerCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.progress.LoggingLevel;
import com.ajaxjs.mcp.jsonrpc.model.progress.LoggingMessageNotification;
import com.ajaxjs.mcp.jsonrpc.model.prompt.GetPromptRequest;
import com.ajaxjs.mcp.jsonrpc.model.prompt.GetPromptResult;
import com.ajaxjs.mcp.jsonrpc.model.prompt.ListPromptsResult;
import com.ajaxjs.mcp.jsonrpc.model.prompt.Prompt;
import com.ajaxjs.mcp.jsonrpc.model.request.InitializeRequest;
import com.ajaxjs.mcp.jsonrpc.model.request.InitializeResult;
import com.ajaxjs.mcp.jsonrpc.model.resources.*;
import com.ajaxjs.mcp.jsonrpc.model.tool.CallToolRequest;
import com.ajaxjs.mcp.jsonrpc.model.tool.CallToolResult;
import com.ajaxjs.mcp.jsonrpc.model.tool.ListToolsResult;
import com.ajaxjs.mcp.jsonrpc.model.tool.Tool;
import com.ajaxjs.mcp.server.McpAsyncServerExchange;
import com.ajaxjs.mcp.server.features.AsyncPromptSpecification;
import com.ajaxjs.mcp.server.features.AsyncResourceSpecification;
import com.ajaxjs.mcp.server.features.AsyncToolSpecification;
import com.ajaxjs.mcp.server.features.ServerFeaturesAsync;
import com.ajaxjs.mcp.session.McpServerSession;
import com.ajaxjs.mcp.session.NotificationHandler;
import com.ajaxjs.mcp.session.RequestHandler;
import com.ajaxjs.mcp.transport.McpServerTransportProvider;
import com.ajaxjs.mcp.utils.McpUtils;
import com.ajaxjs.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class AsyncServerImpl extends McpAsyncServer {
    private final McpServerTransportProvider mcpTransportProvider;

    private final ObjectMapper objectMapper;

    private final ServerCapabilities serverCapabilities;

    private final Implementation serverInfo;

    private final CopyOnWriteArrayList<AsyncToolSpecification> tools = new CopyOnWriteArrayList<>();

    private final CopyOnWriteArrayList<ResourceTemplate> resourceTemplates = new CopyOnWriteArrayList<>();

    private final ConcurrentHashMap<String, AsyncResourceSpecification> resources = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, AsyncPromptSpecification> prompts = new ConcurrentHashMap<>();

    private LoggingLevel minLoggingLevel = LoggingLevel.DEBUG;

    private List<String> protocolVersions = Collections.singletonList(LATEST_PROTOCOL_VERSION);

    public AsyncServerImpl(McpServerTransportProvider mcpTransportProvider, ObjectMapper objectMapper, ServerFeaturesAsync features) {
        this.mcpTransportProvider = mcpTransportProvider;
        this.objectMapper = objectMapper;
        this.serverInfo = features.getServerInfo();
        this.serverCapabilities = features.getServerCapabilities();
        this.tools.addAll(features.getTools());
        this.resources.putAll(features.getResources());
        this.resourceTemplates.addAll(features.getResourceTemplates());
        this.prompts.putAll(features.getPrompts());

        Map<String, RequestHandler<?>> requestHandlers = new HashMap<>();

        // Initialize request handlers for standard MCP methods

        // Ping MUST respond with an empty data, but not NULL response.
        requestHandlers.put(METHOD_PING, (exchange, params) -> CompletableFuture.completedFuture(new HashMap<>()));

        // Add tools API handlers if the tool capability is enabled
        if (this.serverCapabilities.getTools() != null) {
            requestHandlers.put(METHOD_TOOLS_LIST, toolsListRequestHandler());
            requestHandlers.put(METHOD_TOOLS_CALL, toolsCallRequestHandler());
        }

        // Add resources API handlers if provided
        if (this.serverCapabilities.getResources() != null) {
            requestHandlers.put(METHOD_RESOURCES_LIST, resourcesListRequestHandler());
            requestHandlers.put(METHOD_RESOURCES_READ, resourcesReadRequestHandler());
            requestHandlers.put(METHOD_RESOURCES_TEMPLATES_LIST, resourceTemplateListRequestHandler());
        }

        // Add prompts API handlers if provider exists
        if (this.serverCapabilities.getPrompts() != null) {
            requestHandlers.put(METHOD_PROMPT_LIST, promptsListRequestHandler());
            requestHandlers.put(METHOD_PROMPT_GET, promptsGetRequestHandler());
        }

        // Add logging API handlers if the logging capability is enabled
        if (this.serverCapabilities.getLogging() != null)
            requestHandlers.put(METHOD_LOGGING_SET_LEVEL, setlogRequestHandler());

        Map<String, NotificationHandler> notificationHandlers = new HashMap<>();
        notificationHandlers.put(METHOD_NOTIFICATION_INITIALIZED, (exchange, params) -> CompletableFuture.completedFuture(null));
        List<BiFunction<McpAsyncServerExchange, List<Root>, CompletableFuture<Void>>> rootsChangeConsumers = features.getRootsChangeConsumers();

        if (McpUtils.isEmpty(rootsChangeConsumers))
            rootsChangeConsumers = Collections.singletonList((exchange, roots) -> CompletableFuture.fromRunnable(() -> log.warn(
                    "Roots list changed notification, but no consumers provided. Roots list changed: {}", roots)));

        notificationHandlers.put(METHOD_NOTIFICATION_ROOTS_LIST_CHANGED, asyncRootsListChangedNotificationHandler(rootsChangeConsumers));
        mcpTransportProvider.setSessionFactory(transport -> new McpServerSession(UUID.randomUUID().toString(), transport,
                this::asyncInitializeRequestHandler, CompletableFuture::empty, requestHandlers, notificationHandlers));
    }

    // ---------------------------------------
    // Lifecycle Management
    // ---------------------------------------
    private CompletableFuture<InitializeResult> asyncInitializeRequestHandler(InitializeRequest initializeRequest) {
        return CompletableFuture.defer(() -> {
            log.info("Client initialize request - Protocol: {}, Capabilities: {}, Info: {}", initializeRequest.getProtocolVersion(), initializeRequest.getCapabilities(), initializeRequest.getClientInfo());

            // The server MUST respond with the highest protocol version it supports
            // if
            // it does not support the requested (e.g. Client) version.
            String serverProtocolVersion = this.protocolVersions.get(this.protocolVersions.size() - 1);

            if (protocolVersions.contains(initializeRequest.getProtocolVersion())) {
                // If the server supports the requested protocol version, it MUST
                // respond
                // with the same version.
                serverProtocolVersion = initializeRequest.getProtocolVersion();
            } else {
                log.warn("Client requested unsupported protocol version: {}, so the server will sugggest the {} version instead", initializeRequest.getProtocolVersion(), serverProtocolVersion);
            }

            return CompletableFuture.completedFuture(new InitializeResult(serverProtocolVersion, serverCapabilities, serverInfo, null));
        });
    }

    @Override
    public CompletableFuture<Void> closeGracefully() {
        return this.mcpTransportProvider.closeGracefully();
    }

    @Override
    public void close() {
        this.mcpTransportProvider.close();
    }

    public NotificationHandler asyncRootsListChangedNotificationHandler(List<BiFunction<McpAsyncServerExchange, List<Root>, CompletableFuture<Void>>> rootsChangeConsumers) {
        return (exchange, params) -> exchange.listRoots()
                .flatMap(listRootsResult -> Flux.fromIterable(rootsChangeConsumers)
                        .flatMap(consumer -> consumer.apply(exchange, listRootsResult.getRoots()))
                        .onErrorResume(error -> {
                            log.error("Error handling roots list change notification", error);
                            return CompletableFuture.completedFuture(null);
                        }).then());
    }

    // ---------------------------------------
    // Tool Management
    // ---------------------------------------

    @Override
    public CompletableFuture<Void> addTool(AsyncToolSpecification toolSpecification) {
        if (toolSpecification == null) {
            return McpUtils.error("Tool specification must not be null");
        }
        if (toolSpecification.getTool() == null) {
            return McpUtils.error("Tool must not be null");
        }
        if (toolSpecification.getCall() == null) {
            return McpUtils.error("Tool call handler must not be null");
        }
        if (this.serverCapabilities.getTools() == null) {
            return McpUtils.error("Server must be configured with tool capabilities");
        }

        return CompletableFuture.defer(() -> {
            // Check for duplicate tool names
            if (this.tools.stream().anyMatch(th -> th.getTool().getName().equals(toolSpecification.getTool().getName())))
                return McpUtils.error("Tool with name '" + toolSpecification.getTool().getName() + "' already exists");

            this.tools.add(toolSpecification);
            log.debug("Added tool handler: {}", toolSpecification.getTool().getName());

            if (this.serverCapabilities.getTools().getListChanged()) {
                return notifyToolsListChanged();
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    @Override
    public CompletableFuture<Void> removeTool(String toolName) {
        if (toolName == null) {
            return McpUtils.error("Tool name must not be null");
        }
        if (serverCapabilities.getTools() == null) {
            return McpUtils.error("Server must be configured with tool capabilities");
        }

        return CompletableFuture.defer(() -> {
            boolean removed = this.tools.removeIf(toolSpecification -> toolSpecification.getTool().getName().equals(toolName));
            if (removed) {
                log.debug("Removed tool handler: {}", toolName);
                if (this.serverCapabilities.getTools().getListChanged())
                    return notifyToolsListChanged();

                return CompletableFuture.completedFuture(null);
            }

            return McpUtils.error("Tool with name '" + toolName + "' not found");
        });
    }

    @Override
    public CompletableFuture<Void> notifyToolsListChanged() {
        return this.mcpTransportProvider.notifyClients(METHOD_NOTIFICATION_TOOLS_LIST_CHANGED, null);
    }

    private RequestHandler<ListToolsResult> toolsListRequestHandler() {
        return (exchange, params) -> {
            List<Tool> tools = this.tools.stream().map(AsyncToolSpecification::getTool).toList();

            return CompletableFuture.completedFuture(new ListToolsResult(tools, null));
        };
    }

    private RequestHandler<CallToolResult> toolsCallRequestHandler() {
        return (exchange, params) -> {
            CallToolRequest callToolRequest = objectMapper.convertValue(params, new TypeReference<CallToolRequest>() {
            });

            Optional<AsyncToolSpecification> toolSpecification = tools.stream()
                    .filter(tr -> callToolRequest.getName().equals(tr.getTool().getName()))
                    .findAny();

            if (!toolSpecification.isPresent())
                return McpUtils.error("Tool not found: " + callToolRequest.getName());

            return toolSpecification.map(tool -> tool.getCall().apply(exchange, callToolRequest.getArguments())).orElse(McpUtils.error("Tool not found: " + callToolRequest.getName()));
        };
    }

    // ---------------------------------------
    // Resource Management
    // ---------------------------------------

    @Override
    public CompletableFuture<Void> addResource(AsyncResourceSpecification resourceSpecification) {
        if (resourceSpecification == null || resourceSpecification.getResource() == null)
            return McpUtils.error("Resource must not be null");

        if (this.serverCapabilities.getResources() == null)
            return McpUtils.error("Server must be configured with resource capabilities");

        return CompletableFuture.defer(() -> {
            if (this.resources.putIfAbsent(resourceSpecification.getResource().getUri(), resourceSpecification) != null) {
                return McpUtils.error(
                        "Resource with URI '" + resourceSpecification.getResource().getUri() + "' already exists"));
            }
            log.debug("Added resource handler: {}", resourceSpecification.getResource().getUri());
            if (this.serverCapabilities.getResources().getListChanged())
                return notifyResourcesListChanged();

            return CompletableFuture.completedFuture(null);
        });
    }

    @Override
    public CompletableFuture<Void> removeResource(String resourceUri) {
        if (resourceUri == null)
            return McpUtils.error("Resource URI must not be null"));

        if (this.serverCapabilities.getResources() == null)
            return McpUtils.error("Server must be configured with resource capabilities");

        return CompletableFuture.defer(() -> {
            AsyncResourceSpecification removed = this.resources.remove(resourceUri);
            if (removed != null) {
                log.debug("Removed resource handler: {}", resourceUri);
                if (this.serverCapabilities.getResources().getListChanged())
                    return notifyResourcesListChanged();

                return CompletableFuture.completedFuture(null);
            }
            return McpUtils.error("Resource with URI '" + resourceUri + "' not found");
        });
    }

    @Override
    public CompletableFuture<Void> notifyResourcesListChanged() {
        return this.mcpTransportProvider.notifyClients(METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED, null);
    }

    private RequestHandler<ListResourcesResult> resourcesListRequestHandler() {
        return (exchange, params) -> {
            List<Resource> resourceList = this.resources.values()
                    .stream()
                    .map(AsyncResourceSpecification::getResource)
                    .toList();

            return CompletableFuture.completedFuture(new ListResourcesResult(resourceList, null));
        };
    }

    private RequestHandler<ListResourceTemplatesResult> resourceTemplateListRequestHandler() {
        return (exchange, params) -> CompletableFuture.completedFuture(new ListResourceTemplatesResult(resourceTemplates, null));

    }

    private RequestHandler<ReadResourceResult> resourcesReadRequestHandler() {
        return (exchange, params) -> {
            ReadResourceRequest resourceRequest = objectMapper.convertValue(params,
                    new TypeReference<ReadResourceRequest>() {
                    });
            String resourceUri = resourceRequest.getUri();
            AsyncResourceSpecification specification = this.resources.get(resourceUri);

            if (specification != null)
                return specification.getReadHandler().apply(exchange, resourceRequest);

            return McpUtils.error("Resource not found: " + resourceUri);
        };
    }

    // ---------------------------------------
    // Prompt Management
    // ---------------------------------------

    @Override
    public CompletableFuture<Void> addPrompt(AsyncPromptSpecification promptSpecification) {
        if (promptSpecification == null)
            return McpUtils.error("Prompt specification must not be null");

        if (serverCapabilities.getPrompts() == null)
            return McpUtils.error("Server must be configured with prompt capabilities");

        return CompletableFuture.defer(() -> {
            AsyncPromptSpecification specification = prompts.putIfAbsent(promptSpecification.getPrompt().getName(), promptSpecification);
            if (specification != null)
                return McpUtils.error("Prompt with name '" + promptSpecification.getPrompt().getName() + "' already exists");

            log.debug("Added prompt handler: {}", promptSpecification.getPrompt().getName());

            // Servers that declared the listChanged capability SHOULD send a
            // notification,
            // when the list of available prompts changes
            if (serverCapabilities.getPrompts().getListChanged())
                return notifyPromptsListChanged();

            return CompletableFuture.completedFuture(null);
        });
    }

    @Override
    public CompletableFuture<Void> removePrompt(String promptName) {
        if (promptName == null)
            return McpUtils.error("Prompt name must not be null");

        if (serverCapabilities.getPrompts() == null)
            return McpUtils.error("Server must be configured with prompt capabilities");

        return CompletableFuture.defer(() -> {
            AsyncPromptSpecification removed = prompts.remove(promptName);

            if (removed != null) {
                log.debug("Removed prompt handler: {}", promptName);

                // Servers that declared the listChanged capability SHOULD send a
                // notification, when the list of available prompts changes
                if (serverCapabilities.getPrompts().getListChanged())
                    return this.notifyPromptsListChanged();

                return CompletableFuture.completedFuture(null);
            }

            return McpUtils.error("Prompt with name '" + promptName + "' not found");
        });
    }

    @Override
    public CompletableFuture<Void> notifyPromptsListChanged() {
        return mcpTransportProvider.notifyClients(METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED, null);
    }

    private RequestHandler<ListPromptsResult> promptsListRequestHandler() {
        return (exchange, params) -> {
            // TODO: Implement pagination
            // PaginatedRequest request = objectMapper.convertValue(params,
            // new TypeReference<PaginatedRequest>() {
            // });

            List<Prompt> promptList = this.prompts.values()
                    .stream()
                    .map(AsyncPromptSpecification::getPrompt)
                    .toList();

            return CompletableFuture.completedFuture(new ListPromptsResult(promptList, null));
        };
    }

    private RequestHandler<GetPromptResult> promptsGetRequestHandler() {
        return (exchange, params) -> {
            GetPromptRequest promptRequest = JsonUtil.fromJson(params, GetPromptRequest.class);

            // Implement prompt retrieval logic here
            AsyncPromptSpecification specification = prompts.get(promptRequest.getName());
            if (specification == null)
                return McpUtils.error("Prompt not found: " + promptRequest.getName());

            return specification.getPromptHandler().apply(exchange, promptRequest);
        };
    }

    // ---------------------------------------
    // Logging Management
    // ---------------------------------------

    @Override
    public CompletableFuture<Void> loggingNotification(LoggingMessageNotification loggingMessageNotification) {

        if (loggingMessageNotification == null) {
            return McpUtils.error("Logging message must not be null");
        }

        Map<String, Object> params = this.objectMapper.convertValue(loggingMessageNotification,
                new TypeReference<Map<String, Object>>() {
                });

        if (loggingMessageNotification.getLevel().level() < minLoggingLevel.level())
            return CompletableFuture.completedFuture(null);

        return this.mcpTransportProvider.notifyClients(METHOD_NOTIFICATION_MESSAGE, params);
    }

    private RequestHandler<Void> setlogRequestHandler() {
        return (exchange, params) -> {
            this.minLoggingLevel = objectMapper.convertValue(params, new TypeReference<LoggingLevel>() {
            });

            return CompletableFuture.completedFuture(null);
        };
    }

    // ---------------------------------------
    // Sampling
    // ---------------------------------------

    @Override
    void setProtocolVersions(List<String> protocolVersions) {
        this.protocolVersions = protocolVersions;
    }

}