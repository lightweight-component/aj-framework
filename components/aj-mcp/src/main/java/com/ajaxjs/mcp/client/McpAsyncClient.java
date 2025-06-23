/*
 * Copyright 2024-2024 the original author or authors.
 */
package com.ajaxjs.mcp.client;

import com.ajaxjs.mcp.McpConstant;
import com.ajaxjs.mcp.client.features.ClientAsync;
import com.ajaxjs.mcp.client.mcpclient.McpClient;
import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.ListRootsResult;
import com.ajaxjs.mcp.jsonrpc.model.Root;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.ClientCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.server.ServerCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.pagination.PaginatedRequest;
import com.ajaxjs.mcp.jsonrpc.model.progress.LoggingLevel;
import com.ajaxjs.mcp.jsonrpc.model.progress.LoggingMessageNotification;
import com.ajaxjs.mcp.jsonrpc.model.prompt.GetPromptRequest;
import com.ajaxjs.mcp.jsonrpc.model.prompt.GetPromptResult;
import com.ajaxjs.mcp.jsonrpc.model.prompt.ListPromptsResult;
import com.ajaxjs.mcp.jsonrpc.model.prompt.Prompt;
import com.ajaxjs.mcp.jsonrpc.model.request.InitializeRequest;
import com.ajaxjs.mcp.jsonrpc.model.request.InitializeResult;
import com.ajaxjs.mcp.jsonrpc.model.resources.*;
import com.ajaxjs.mcp.jsonrpc.model.sampling.CreateMessageRequest;
import com.ajaxjs.mcp.jsonrpc.model.sampling.CreateMessageResult;
import com.ajaxjs.mcp.jsonrpc.model.tool.CallToolRequest;
import com.ajaxjs.mcp.jsonrpc.model.tool.CallToolResult;
import com.ajaxjs.mcp.jsonrpc.model.tool.ListToolsResult;
import com.ajaxjs.mcp.jsonrpc.model.tool.Tool;
import com.ajaxjs.mcp.model.McpError;
import com.ajaxjs.mcp.session.McpClientSession;
import com.ajaxjs.mcp.session.NotificationHandler;
import com.ajaxjs.mcp.session.RequestHandler;
import com.ajaxjs.mcp.transport.McpClientTransport;
import com.ajaxjs.mcp.transport.McpTransport;
import com.ajaxjs.mcp.utils.McpUtils;
import com.ajaxjs.util.ObjectHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Model Context Protocol (MCP) client implementation that provides asynchronous
 * communication with MCP servers using Project Reactor's Mono and Flux types.
 *
 * <p>
 * This client implements the MCP specification, enabling AI models to interact with
 * external tools and resources through a standardized interface. Key features include:
 * <ul>
 * <li>Asynchronous communication using reactive programming patterns
 * <li>Tool discovery and invocation for server-provided functionality
 * <li>Resource access and management with URI-based addressing
 * <li>Prompt template handling for standardized AI interactions
 * <li>Real-time notifications for tools, resources, and prompts changes
 * <li>Structured logging with configurable severity levels
 * <li>Message sampling for AI model interactions
 * </ul>
 *
 * <p>
 * The client follows a lifecycle:
 * <ol>
 * <li>Initialization - Establishes connection and negotiates capabilities
 * <li>Normal Operation - Handles requests and notifications
 * <li>Graceful Shutdown - Ensures clean connection termination
 * </ol>
 *
 * <p>
 * This implementation uses Project Reactor for non-blocking operations, making it
 * suitable for high-throughput scenarios and reactive applications. All operations return
 * Mono or Flux types that can be composed into reactive pipelines.
 *
 * @see McpClient
 * @see McpClientSession
 */
@Slf4j
public class McpAsyncClient implements McpConstant {

    private static TypeReference<Void> VOID_TYPE_REFERENCE = new TypeReference<Void>() {
    };

    protected final Sinks.One<InitializeResult> initializedSink = Sinks.one();

    private AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * The max timeout to await for the client-server connection to be initialized.
     */
    private final Duration initializationTimeout;

    /**
     * The MCP session implementation that manages bidirectional JSON-RPC communication
     * between clients and servers.
     */
    private final McpClientSession mcpSession;

    /**
     * Client capabilities.
     */
    private final ClientCapabilities clientCapabilities;

    /**
     * Client implementation information.
     */
    private final Implementation clientInfo;

    /**
     * Server capabilities.
     */
    private ServerCapabilities serverCapabilities;

    /**
     * Server implementation information.
     */
    private Implementation serverInfo;

    /**
     * Roots define the boundaries of where servers can operate within the filesystem,
     * allowing them to understand which directories and files they have access to.
     * Servers can request the list of roots from supporting clients and receive
     * notifications when that list changes.
     */
    private final ConcurrentHashMap<String, Root> roots;

    /**
     * MCP provides a standardized way for servers to request LLM sampling ("completions"
     * or "generations") from language models via clients. This flow allows clients to
     * maintain control over model access, selection, and permissions while enabling
     * servers to leverage AI capabilities—with no server API keys necessary. Servers can
     * request text or image-based interactions and optionally include context from MCP
     * servers in their prompts.
     */
    private Function<CreateMessageRequest, CompletableFuture<CreateMessageResult>> samplingHandler;

    /**
     * Client transport implementation.
     */
    private final McpTransport transport;

    /**
     * Supported protocol versions.
     */
    private List<String> protocolVersions = Collections.singletonList(LATEST_PROTOCOL_VERSION);

    /**
     * Create a new McpAsyncClient with the given transport and session request-response
     * timeout.
     *
     * @param transport             the transport to use.
     * @param requestTimeout        the session request-response timeout.
     * @param initializationTimeout the max timeout to await for the client-server
     * @param features              the MCP Client supported features.
     */
    public McpAsyncClient(McpClientTransport transport, Duration requestTimeout, Duration initializationTimeout, ClientAsync features) {
        Objects.requireNonNull(transport, "Transport must not be null");
        Objects.requireNonNull(requestTimeout, "Request timeout must not be null");
        Objects.requireNonNull(initializationTimeout, "Initialization timeout must not be null");

        this.clientInfo = features.getClientInfo();
        this.clientCapabilities = features.getClientCapabilities();
        this.transport = transport;
        this.roots = new ConcurrentHashMap<>(features.getRoots());
        this.initializationTimeout = initializationTimeout;

        // Request Handlers
        Map<String, RequestHandler<?>> requestHandlers = new HashMap<>();

        // Roots List Request Handler
        if (clientCapabilities.getRoots() != null)
            requestHandlers.put(METHOD_ROOTS_LIST, rootsListRequestHandler());

        // Sampling Handler
        if (clientCapabilities.getSampling() != null) {
            if (features.getSamplingHandler() == null)
                throw new McpError("Sampling handler must not be null when client capabilities include sampling");

            this.samplingHandler = features.getSamplingHandler();
            requestHandlers.put(METHOD_SAMPLING_CREATE_MESSAGE, samplingCreateMessageHandler());
        }

        // Notification Handlers
        Map<String, NotificationHandler> notificationHandlers = new HashMap<>();

        // Tools Change Notification
        List<Function<List<Tool>, CompletableFuture<Void>>> toolsChangeConsumersFinal = new ArrayList<>();
        toolsChangeConsumersFinal
                .add((notification) -> Mono.fromRunnable(() -> log.debug("Tools changed: {}", notification)));

        if (!McpUtils.isEmpty(features.getToolsChangeConsumers())) {
            toolsChangeConsumersFinal.addAll(features.getToolsChangeConsumers());
        }
        notificationHandlers.put(METHOD_NOTIFICATION_TOOLS_LIST_CHANGED, asyncToolsChangeNotificationHandler(toolsChangeConsumersFinal));

        // Resources Change Notification
        List<Function<List<Resource>, CompletableFuture<Void>>> resourcesChangeConsumersFinal = new ArrayList<>();
        resourcesChangeConsumersFinal.add((notification) -> Mono.fromRunnable(() -> log.debug("Resources changed: {}", notification)));

        if (!McpUtils.isEmpty(features.getResourcesChangeConsumers()))
            resourcesChangeConsumersFinal.addAll(features.getResourcesChangeConsumers());

        notificationHandlers.put(METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED, asyncResourcesChangeNotificationHandler(resourcesChangeConsumersFinal));

        // Prompts Change Notification
        List<Function<List<Prompt>, CompletableFuture<Void>>> promptsChangeConsumersFinal = new ArrayList<>();
        promptsChangeConsumersFinal.add((notification) -> Mono.fromRunnable(() -> log.debug("Prompts changed: {}", notification)));

        if (!McpUtils.isEmpty(features.getPromptsChangeConsumers()))
            promptsChangeConsumersFinal.addAll(features.etPromptsChangeConsumers());

        notificationHandlers.put(METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED,
                asyncPromptsChangeNotificationHandler(promptsChangeConsumersFinal));

        // Utility Logging Notification
        List<Function<LoggingMessageNotification, CompletableFuture<Void>>> loggingConsumersFinal = new ArrayList<>();
        loggingConsumersFinal.add((notification) -> Mono.fromRunnable(() -> log.debug("Logging: {}", notification)));
        if (!McpUtils.isEmpty(features.getLoggingConsumers()))
            loggingConsumersFinal.addAll(features.getLoggingConsumers());

        notificationHandlers.put(METHOD_NOTIFICATION_MESSAGE,
                asyncLoggingNotificationHandler(loggingConsumersFinal));

        this.mcpSession = new McpClientSession(requestTimeout, transport, requestHandlers, notificationHandlers);

    }

    /**
     * Get the server capabilities that define the supported features and functionality.
     *
     * @return The server capabilities
     */
    public ServerCapabilities getServerCapabilities() {
        return this.serverCapabilities;
    }

    /**
     * Get the server implementation information.
     *
     * @return The server implementation details
     */
    public Implementation getServerInfo() {
        return this.serverInfo;
    }

    /**
     * Check if the client-server connection is initialized.
     *
     * @return true if the client-server connection is initialized
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Get the client capabilities that define the supported features and functionality.
     *
     * @return The client capabilities
     */
    public ClientCapabilities getClientCapabilities() {
        return clientCapabilities;
    }

    /**
     * Get the client implementation information.
     *
     * @return The client implementation details
     */
    public Implementation getClientInfo() {
        return clientInfo;
    }

    /**
     * Closes the client connection immediately.
     */
    public void close() {
        this.mcpSession.close();
    }

    /**
     * Gracefully closes the client connection.
     *
     * @return A Mono that completes when the connection is closed
     */
    public CompletableFuture<Void> closeGracefully() {
        return this.mcpSession.closeGracefully();
    }

    // --------------------------
    // Initialization
    // --------------------------

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
     * The protocol version the client supports, client's capabilities and clients
     * implementation information.
     * <p/>
     * The server MUST respond with its own capabilities and information.
     * <p/>
     * After successful initialization, the client MUST send an initialized notification
     * to indicate it is ready to begin normal operations.
     *
     * @return the initialize result.
     * @see <a href=
     * "https://github.com/modelcontextprotocol/specification/blob/main/docs/specification/basic/lifecycle.md#initialization">MCP
     * Initialization Spec</a>
     */
    public CompletableFuture<InitializeResult> initialize() {
        String latestVersion = this.protocolVersions.get(this.protocolVersions.size() - 1);
        InitializeRequest initializeRequest = new InitializeRequest(latestVersion, clientCapabilities, clientInfo);
        CompletableFuture<InitializeResult> result = mcpSession.sendRequest(METHOD_INITIALIZE, initializeRequest, new TypeReference<InitializeResult>() {
        });

        return result.flatMap(initializeResult -> {
            this.serverCapabilities = initializeResult.getCapabilities();
            this.serverInfo = initializeResult.getServerInfo();

            log.info("Server response with Protocol: {}, Capabilities: {}, Info: {} and Instructions {}",
                    initializeResult.protocolVersion(), initializeResult.capabilities(), initializeResult.serverInfo(),
                    initializeResult.instructions());

            if (!protocolVersions.contains(initializeResult.protocolVersion())) {
                return McpUtils.error("Unsupported protocol version from the server: " + initializeResult.getProtocolVersion());
            }

            return this.mcpSession.sendNotification(METHOD_NOTIFICATION_INITIALIZED, null).doOnSuccess(v -> {
                this.initialized.set(true);
                this.initializedSink.tryEmitValue(initializeResult);
            }).thenReturn(initializeResult);
        });
    }

    /**
     * Utility method to handle the common pattern of checking initialization before
     * executing an operation.
     *
     * @param <T>        The type of the result Mono
     * @param actionName The action to perform if the client is initialized
     * @param operation  The operation to execute if the client is initialized
     * @return A Mono that completes with the result of the operation
     */
    private <T> CompletableFuture<T> withInitializationCheck(String actionName, Function<InitializeResult, CompletableFuture<T>> operation) {
        return initializedSink.asMono()
                .timeout(initializationTimeout)
                .onErrorResume(TimeoutException.class, ex -> McpUtils.error("Client must be initialized before " + actionName))
                .flatMap(operation);
    }

    // --------------------------
    // Basic Utilites
    // --------------------------

    /**
     * Sends a ping request to the server.
     *
     * @return A Mono that completes with the server's ping response
     */
    public CompletableFuture<Object> ping() {
        return this.withInitializationCheck("pinging the server", initializedResult -> this.mcpSession.sendRequest(METHOD_PING, null, new TypeReference<Object>() {}));
    }

    // --------------------------
    // Roots
    // --------------------------

    /**
     * Adds a new root to the client's root list.
     *
     * @param root The root to add.
     * @return A Mono that completes when the root is added and notifications are sent.
     */
    public CompletableFuture<Void> addRoot(Root root) {
        if (root == null)
            return McpUtils.error("Root must not be null");

        if (clientCapabilities.getRoots() == null)
            return McpUtils.error("Client must be configured with roots capabilities");

        if (roots.containsKey(root.getUri()))
            return McpUtils.error("Root with uri '" + root.getUri() + "' already exists");

        this.roots.put(root.getUri(), root);
        log.debug("Added root: {}", root);

        if (clientCapabilities.getRoots().getListChanged()) {
            if (isInitialized()) 
                return rootsListChangedNotification();
             else 
                log.warn("Client is not initialized, ignore sending a roots list changed notification");
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Removes a root from the client's root list.
     *
     * @param rootUri The URI of the root to remove.
     * @return A Mono that completes when the root is removed and notifications are sent.
     */
    public CompletableFuture<Void> removeRoot(String rootUri) {
        if (rootUri == null)
            return McpUtils.error("Root uri must not be null");

        if (this.clientCapabilities.getRoots() == null)
            return McpUtils.error("Client must be configured with roots capabilities");

        Root removed = this.roots.remove(rootUri);

        if (removed != null) {
            log.debug("Removed Root: {}", rootUri);

            if (this.clientCapabilities.getRoots().getListChanged()) {
                if (this.isInitialized())
                    return this.rootsListChangedNotification();
                else
                    log.warn("Client is not initialized, ignore sending a roots list changed notification");
            }

            return CompletableFuture.completedFuture(null);
        }

        return McpUtils.error("Root with uri '" + rootUri + "' not found");
    }

    /**
     * Manually sends a roots/list_changed notification. The addRoot and removeRoot
     * methods automatically send the roots/list_changed notification if the client is in
     * an initialized state.
     *
     * @return A Mono that completes when the notification is sent.
     */
    public CompletableFuture<Void> rootsListChangedNotification() {
        return withInitializationCheck("sending roots list changed notification", initResult -> mcpSession.sendNotification(METHOD_NOTIFICATION_ROOTS_LIST_CHANGED));
    }

    private RequestHandler<ListRootsResult> rootsListRequestHandler() {
        return params -> {
            @SuppressWarnings("unused")
            PaginatedRequest request = transport.unmarshalFrom(params, new TypeReference<PaginatedRequest>() {});
            List<Root> roots = new ArrayList<>(this.roots.values());

            return CompletableFuture.completedFuture(new ListRootsResult(roots));
        };
    }

    // --------------------------
    // Sampling
    // --------------------------
    private RequestHandler<CreateMessageResult> samplingCreateMessageHandler() {
        return params -> {
            CreateMessageRequest request = transport.unmarshalFrom(params, new TypeReference<CreateMessageRequest>() {});

            return samplingHandler.apply(request);
        };
    }

    // --------------------------
    // Tools
    // --------------------------
    private static final TypeReference<CallToolResult> CALL_TOOL_RESULT_TYPE_REF = new TypeReference<CallToolResult>() {
    };

    private static final TypeReference<ListToolsResult> LIST_TOOLS_RESULT_TYPE_REF = new TypeReference<ListToolsResult>() {
    };

    /**
     * Calls a tool provided by the server. Tools enable servers to expose executable
     * functionality that can interact with external systems, perform computations, and
     * take actions in the real world.
     *
     * @param callToolRequest The request containing the tool name and input parameters.
     * @return A Mono that emits the result of the tool call, including the output and any
     * errors.
     * @see CallToolRequest
     * @see CallToolResult
     * @see #listTools()
     */
    public CompletableFuture<CallToolResult> callTool(CallToolRequest callToolRequest) {
        return this.withInitializationCheck("calling tools", initializedResult -> {
            if (serverCapabilities.getTools() == null)
                return McpUtils.error("Server does not provide tools capability");

            return mcpSession.sendRequest(METHOD_TOOLS_CALL, callToolRequest, CALL_TOOL_RESULT_TYPE_REF);
        });
    }

    /**
     * Retrieves the list of all tools provided by the server.
     *
     * @return A Mono that emits the list of tools result.
     */
    public CompletableFuture<ListToolsResult> listTools() {
        return listTools(null);
    }

    /**
     * Retrieves a paginated list of tools provided by the server.
     *
     * @param cursor Optional pagination cursor from a previous list request
     * @return A Mono that emits the list of tools result
     */
    public CompletableFuture<ListToolsResult> listTools(String cursor) {
        return withInitializationCheck("listing tools", initializedResult -> {
            if (serverCapabilities.getTools() == null)
                return McpUtils.error("Server does not provide tools capability");

            return mcpSession.sendRequest(METHOD_TOOLS_LIST, new PaginatedRequest(cursor), LIST_TOOLS_RESULT_TYPE_REF);
        });
    }

    private NotificationHandler asyncToolsChangeNotificationHandler(List<Function<List<Tool>, CompletableFuture<Void>>> toolsChangeConsumers) {
        // TODO: params are not used yet
        return params -> listTools().flatMap(listToolsResult -> Flux.fromIterable(toolsChangeConsumers)
                .flatMap(consumer -> consumer.apply(listToolsResult.getTools()))
                .onErrorResume(error -> {
                    log.error("Error handling tools list change notification", error);
                    return CompletableFuture.completedFuture(null);
                }).then());
    }

    // --------------------------
    // Resources
    // --------------------------

    private static final TypeReference<ListResourcesResult> LIST_RESOURCES_RESULT_TYPE_REF = new TypeReference<ListResourcesResult>() {
    };

    private static final TypeReference<ReadResourceResult> READ_RESOURCE_RESULT_TYPE_REF = new TypeReference<ReadResourceResult>() {
    };

    private static final TypeReference<ListResourceTemplatesResult> LIST_RESOURCE_TEMPLATES_RESULT_TYPE_REF = new TypeReference<ListResourceTemplatesResult>() {
    };

    /**
     * Retrieves the list of all resources provided by the server. Resources represent any
     * kind of UTF-8 encoded data that an MCP server makes available to clients, such as
     * database records, API responses, log files, and more.
     *
     * @return A Mono that completes with the list of resources result.
     * @see ListResourcesResult
     * @see #readResource(Resource)
     */
    public CompletableFuture<ListResourcesResult> listResources() {
        return listResources(null);
    }

    /**
     * Retrieves a paginated list of resources provided by the server. Resources represent
     * any kind of UTF-8 encoded data that an MCP server makes available to clients, such
     * as database records, API responses, log files, and more.
     *
     * @param cursor Optional pagination cursor from a previous list request.
     * @return A Mono that completes with the list of resources result.
     * @see ListResourcesResult
     * @see #readResource(Resource)
     */
    public CompletableFuture<ListResourcesResult> listResources(String cursor) {
        return withInitializationCheck("listing resources", initializedResult -> {
            if (serverCapabilities.getResources() == null)
                return McpUtils.error("Server does not provide the resources capability");

            return mcpSession.sendRequest(METHOD_RESOURCES_LIST, new PaginatedRequest(cursor), LIST_RESOURCES_RESULT_TYPE_REF);
        });
    }

    /**
     * Reads the content of a specific resource identified by the provided Resource
     * object. This method fetches the actual data that the resource represents.
     *
     * @param resource The resource to read, containing the URI that identifies the
     *                 resource.
     * @return A Mono that completes with the resource content.
     * @see Resource
     * @see ReadResourceResult
     */
    public CompletableFuture<ReadResourceResult> readResource(Resource resource) {
        return readResource(new ReadResourceRequest(resource.getUri()));
    }

    /**
     * Reads the content of a specific resource identified by the provided request. This
     * method fetches the actual data that the resource represents.
     *
     * @param readResourceRequest The request containing the URI of the resource to read
     * @return A Mono that completes with the resource content.
     * @see ReadResourceRequest
     * @see ReadResourceResult
     */
    public CompletableFuture<ReadResourceResult> readResource(ReadResourceRequest readResourceRequest) {
        return withInitializationCheck("reading resources", initializedResult -> {
            if (serverCapabilities.getResources() == null)
                return McpUtils.error("Server does not provide the resources capability");

            return mcpSession.sendRequest(METHOD_RESOURCES_READ, readResourceRequest, READ_RESOURCE_RESULT_TYPE_REF);
        });
    }

    /**
     * Retrieves the list of all resource templates provided by the server. Resource
     * templates allow servers to expose parameterized resources using URI templates,
     * enabling dynamic resource access based on variable parameters.
     *
     * @return A Mono that completes with the list of resource templates result.
     * @see ListResourceTemplatesResult
     */
    public CompletableFuture<ListResourceTemplatesResult> listResourceTemplates() {
        return this.listResourceTemplates(null);
    }

    /**
     * Retrieves a paginated list of resource templates provided by the server. Resource
     * templates allow servers to expose parameterized resources using URI templates,
     * enabling dynamic resource access based on variable parameters.
     *
     * @param cursor Optional pagination cursor from a previous list request.
     * @return A Mono that completes with the list of resource templates result.
     * @see ListResourceTemplatesResult
     */
    public CompletableFuture<ListResourceTemplatesResult> listResourceTemplates(String cursor) {
        return withInitializationCheck("listing resource templates", initializedResult -> {
            if (serverCapabilities.getResources() == null)
                return McpUtils.error("Server does not provide the resources capability");

            return mcpSession.sendRequest(METHOD_RESOURCES_TEMPLATES_LIST, new PaginatedRequest(cursor), LIST_RESOURCE_TEMPLATES_RESULT_TYPE_REF);
        });
    }

    /**
     * Subscribes to changes in a specific resource. When the resource changes on the
     * server, the client will receive notifications through the resources change
     * notification handler.
     *
     * @param subscribeRequest The subscribe request containing the URI of the resource.
     * @return A Mono that completes when the subscription is complete.
     * @see SubscribeRequest
     * @see #unsubscribeResource(UnsubscribeRequest)
     */
    public CompletableFuture<Void> subscribeResource(SubscribeRequest subscribeRequest) {
        return this.withInitializationCheck("subscribing to resources", initializedResult -> this.mcpSession
                .sendRequest(METHOD_RESOURCES_SUBSCRIBE, subscribeRequest, VOID_TYPE_REFERENCE));
    }

    /**
     * Cancels an existing subscription to a resource. After unsubscribing, the client
     * will no longer receive notifications when the resource changes.
     *
     * @param unsubscribeRequest The unsubscribe request containing the URI of the
     *                           resource.
     * @return A Mono that completes when the unsubscription is complete.
     * @see UnsubscribeRequest
     * @see #subscribeResource(SubscribeRequest)
     */
    public CompletableFuture<Void> unsubscribeResource(UnsubscribeRequest unsubscribeRequest) {
        return withInitializationCheck("unsubscribing from resources", initializedResult -> mcpSession.sendRequest(METHOD_RESOURCES_UNSUBSCRIBE, unsubscribeRequest, VOID_TYPE_REFERENCE));
    }

    private NotificationHandler asyncResourcesChangeNotificationHandler(List<Function<List<Resource>, CompletableFuture<Void>>> resourcesChangeConsumers) {
        return params -> listResources().flatMap(listResourcesResult -> Flux.fromIterable(resourcesChangeConsumers)
                .flatMap(consumer -> consumer.apply(listResourcesResult.resources()))
                .onErrorResume(error -> {
                    log.error("Error handling resources list change notification", error);
                    return CompletableFuture.completedFuture(null);
                })
                .then());
    }

    // --------------------------
    // Prompts
    // --------------------------
    private static final TypeReference<ListPromptsResult> LIST_PROMPTS_RESULT_TYPE_REF = new TypeReference<ListPromptsResult>() {
    };

    private static final TypeReference<GetPromptResult> GET_PROMPT_RESULT_TYPE_REF = new TypeReference<GetPromptResult>() {
    };

    /**
     * Retrieves the list of all prompts provided by the server.
     *
     * @return A Mono that completes with the list of prompts result.
     * @see ListPromptsResult
     * @see #getPrompt(GetPromptRequest)
     */
    public CompletableFuture<ListPromptsResult> listPrompts() {
        return listPrompts(null);
    }

    /**
     * Retrieves a paginated list of prompts provided by the server.
     *
     * @param cursor Optional pagination cursor from a previous list request
     * @return A Mono that completes with the list of prompts result.
     * @see ListPromptsResult
     * @see #getPrompt(GetPromptRequest)
     */
    public CompletableFuture<ListPromptsResult> listPrompts(String cursor) {
        return this.withInitializationCheck("listing prompts", initializedResult -> this.mcpSession
                .sendRequest(METHOD_PROMPT_LIST, new PaginatedRequest(cursor), LIST_PROMPTS_RESULT_TYPE_REF));
    }

    /**
     * Retrieves a specific prompt by its ID. This provides the complete prompt template
     * including all parameters and instructions for generating AI content.
     *
     * @param getPromptRequest The request containing the ID of the prompt to retrieve.
     * @return A Mono that completes with the prompt result.
     * @see GetPromptRequest
     * @see GetPromptResult
     * @see #listPrompts()
     */
    public CompletableFuture<GetPromptResult> getPrompt(GetPromptRequest getPromptRequest) {
        return this.withInitializationCheck("getting prompts", initializedResult -> this.mcpSession
                .sendRequest(METHOD_PROMPT_GET, getPromptRequest, GET_PROMPT_RESULT_TYPE_REF));
    }

    private NotificationHandler asyncPromptsChangeNotificationHandler(List<Function<List<Prompt>, CompletableFuture<Void>>> promptsChangeConsumers) {
        return params -> listPrompts().flatMap(listPromptsResult -> Flux.fromIterable(promptsChangeConsumers)
                .flatMap(consumer -> consumer.apply(listPromptsResult.getPrompts()))
                .onErrorResume(error -> {
                    log.error("Error handling prompts list change notification", error);
                    return Mono.empty();
                }).then());
    }

    // --------------------------
    // Logging
    // --------------------------
    private NotificationHandler asyncLoggingNotificationHandler(List<Function<LoggingMessageNotification, CompletableFuture<Void>>> loggingConsumers) {

        return params -> {
            LoggingMessageNotification loggingMessageNotification = transport.unmarshalFrom(params, new TypeReference<LoggingMessageNotification>() {
            });

            return Flux.fromIterable(loggingConsumers).flatMap(consumer -> consumer.apply(loggingMessageNotification)).then();
        };
    }

    /**
     * Sets the minimum logging level for messages received from the server. The client
     * will only receive log messages at or above the specified severity level.
     *
     * @param loggingLevel The minimum logging level to receive.
     * @return A Mono that completes when the logging level is set.
     * @see LoggingLevel
     */
    public CompletableFuture<Void> setLoggingLevel(LoggingLevel loggingLevel) {
        if (loggingLevel == null)
            return McpUtils.error("Logging level must not be null");

        return withInitializationCheck("setting logging level", initializedResult -> {
            String levelName = transport.unmarshalFrom(loggingLevel, new TypeReference<String>() {
            });
            Map<String, Object> params = ObjectHelper.mapOf("level", levelName);

            return mcpSession.sendNotification(METHOD_LOGGING_SET_LEVEL, params);
        });
    }

    /**
     * This method is package-private and used for test only. Should not be called by user
     * code.
     *
     * @param protocolVersions the Client supported protocol versions.
     */
    void setProtocolVersions(List<String> protocolVersions) {
        this.protocolVersions = protocolVersions;
    }

}
