package com.ajaxjs.mcp.client.mcpclient;

import com.ajaxjs.mcp.client.McpAsyncClient;
import com.ajaxjs.mcp.client.McpSyncClient;
import com.ajaxjs.mcp.client.features.ClientAsync;
import com.ajaxjs.mcp.client.features.ClientSync;
import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.Root;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.ClientCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.progress.LoggingMessageNotification;
import com.ajaxjs.mcp.jsonrpc.model.prompt.Prompt;
import com.ajaxjs.mcp.jsonrpc.model.resources.Resource;
import com.ajaxjs.mcp.jsonrpc.model.sampling.CreateMessageRequest;
import com.ajaxjs.mcp.jsonrpc.model.sampling.CreateMessageResult;
import com.ajaxjs.mcp.jsonrpc.model.tool.Tool;
import com.ajaxjs.mcp.transport.McpClientTransport;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Synchronous client specification. This class follows the builder pattern to provide
 * a fluent API for setting up clients with custom configurations.
 *
 * <p>
 * The builder supports configuration of:
 * <ul>
 * <li>Transport layer for client-server communication
 * <li>Request timeouts for operation boundaries
 * <li>Client capabilities for feature negotiation
 * <li>Client implementation details for version tracking
 * <li>Root URIs for resource access
 * <li>Change notification handlers for tools, resources, and prompts
 * <li>Custom message sampling logic
 * </ul>
 */
public class SyncSpec {
    private final McpClientTransport transport;

    private Duration requestTimeout = Duration.ofSeconds(20); // Default timeout

    private Duration initializationTimeout = Duration.ofSeconds(20);

    private ClientCapabilities capabilities;

    private Implementation clientInfo = new Implementation("Java SDK MCP Client", "1.0.0");

    private final Map<String, Root> roots = new HashMap<>();

    private final List<Consumer<List<Tool>>> toolsChangeConsumers = new ArrayList<>();

    private final List<Consumer<List<Resource>>> resourcesChangeConsumers = new ArrayList<>();

    private final List<Consumer<List<Prompt>>> promptsChangeConsumers = new ArrayList<>();

    private final List<Consumer<LoggingMessageNotification>> loggingConsumers = new ArrayList<>();

    private Function<CreateMessageRequest, CreateMessageResult> samplingHandler;

    public SyncSpec(McpClientTransport transport) {
        Objects.requireNonNull(transport, "Transport must not be null");
        this.transport = transport;
    }

    /**
     * Sets the duration to wait for server responses before timing out requests. This
     * timeout applies to all requests made through the client, including tool calls,
     * resource access, and prompt operations.
     *
     * @param requestTimeout The duration to wait before timing out requests. Must not be null.
     * @return This builder instance for method chaining
     * @throws IllegalArgumentException if requestTimeout is null
     */
    public SyncSpec requestTimeout(Duration requestTimeout) {
        Objects.requireNonNull(requestTimeout, "Request timeout must not be null");
        this.requestTimeout = requestTimeout;
        return this;
    }

    /**
     * @param initializationTimeout The duration to wait for the initializaiton lifecycle step to complete.
     * @return This builder instance for method chaining
     * @throws IllegalArgumentException if initializationTimeout is null
     */
    public SyncSpec initializationTimeout(Duration initializationTimeout) {
        Objects.requireNonNull(initializationTimeout, "Initialization timeout must not be null");
        this.initializationTimeout = initializationTimeout;

        return this;
    }

    /**
     * Sets the client capabilities that will be advertised to the server during
     * connection initialization. Capabilities define what features the client
     * supports, such as tool execution, resource access, and prompt handling.
     *
     * @param capabilities The client capabilities configuration. Must not be null.
     * @return This builder instance for method chaining
     * @throws IllegalArgumentException if capabilities is null
     */
    public SyncSpec capabilities(ClientCapabilities capabilities) {
        Objects.requireNonNull(capabilities, "Capabilities must not be null");
        this.capabilities = capabilities;
        return this;
    }

    /**
     * Sets the client implementation information that will be shared with the server
     * during connection initialization. This helps with version compatibility and
     * debugging.
     *
     * @param clientInfo The client implementation details including name and version.
     *                   Must not be null.
     * @return This builder instance for method chaining
     * @throws IllegalArgumentException if clientInfo is null
     */
    public SyncSpec clientInfo(Implementation clientInfo) {
        Objects.requireNonNull(clientInfo, "Client info must not be null");
        this.clientInfo = clientInfo;
        return this;
    }

    /**
     * Sets the root URIs that this client can access. Roots define the base URIs for
     * resources that the client can request from the server. For example, a root
     * might be "file://workspace" for accessing workspace files.
     *
     * @param roots A list of root definitions. Must not be null.
     * @return This builder instance for method chaining
     * @throws IllegalArgumentException if roots is null
     */
    public SyncSpec roots(List<Root> roots) {
        Objects.requireNonNull(roots, "Roots must not be null");
        for (Root root : roots)
            this.roots.put(root.getUri(), root);

        return this;
    }

    /**
     * Sets the root URIs that this client can access, using a varargs parameter for
     * convenience. This is an alternative to {@link #roots(List)}.
     *
     * @param roots An array of root definitions. Must not be null.
     * @return This builder instance for method chaining
     * @throws IllegalArgumentException if roots is null
     * @see #roots(List)
     */
    public SyncSpec roots(Root... roots) {
        Objects.requireNonNull(roots, "Roots must not be null");
        for (Root root : roots)
            this.roots.put(root.getUri(), root);

        return this;
    }

    /**
     * Sets a custom sampling handler for processing message creation requests. The
     * sampling handler can modify or validate messages before they are sent to the
     * server, enabling custom processing logic.
     *
     * @param samplingHandler A function that processes message requests and returns results. Must not be null.
     * @return This builder instance for method chaining
     * @throws IllegalArgumentException if samplingHandler is null
     */
    public SyncSpec sampling(Function<CreateMessageRequest, CreateMessageResult> samplingHandler) {
        Objects.requireNonNull(samplingHandler, "Sampling handler must not be null");
        this.samplingHandler = samplingHandler;

        return this;
    }

    /**
     * Adds a consumer to be notified when the available tools change. This allows the
     * client to react to changes in the server's tool capabilities, such as tools
     * being added or removed.
     *
     * @param toolsChangeConsumer A consumer that receives the updated list of available tools. Must not be null.
     * @return This builder instance for method chaining
     * @throws IllegalArgumentException if toolsChangeConsumer is null
     */
    public SyncSpec toolsChangeConsumer(Consumer<List<Tool>> toolsChangeConsumer) {
        Objects.requireNonNull(toolsChangeConsumer, "Tools change consumer must not be null");
        toolsChangeConsumers.add(toolsChangeConsumer);

        return this;
    }

    /**
     * Adds a consumer to be notified when the available resources change. This allows
     * the client to react to changes in the server's resource availability, such as
     * files being added or removed.
     *
     * @param resourcesChangeConsumer A consumer that receives the updated list of available resources. Must not be null.
     * @return This builder instance for method chaining
     * @throws IllegalArgumentException if resourcesChangeConsumer is null
     */
    public SyncSpec resourcesChangeConsumer(Consumer<List<Resource>> resourcesChangeConsumer) {
        Objects.requireNonNull(resourcesChangeConsumer, "Resources change consumer must not be null");
        resourcesChangeConsumers.add(resourcesChangeConsumer);

        return this;
    }

    /**
     * Adds a consumer to be notified when the available prompts change. This allows
     * the client to react to changes in the server's prompt templates, such as new
     * templates being added or existing ones being modified.
     *
     * @param promptsChangeConsumer A consumer that receives the updated list of available prompts. Must not be null.
     * @return This builder instance for method chaining
     * @throws IllegalArgumentException if promptsChangeConsumer is null
     */
    public SyncSpec promptsChangeConsumer(Consumer<List<Prompt>> promptsChangeConsumer) {
        Objects.requireNonNull(promptsChangeConsumer, "Prompts change consumer must not be null");
        promptsChangeConsumers.add(promptsChangeConsumer);

        return this;
    }

    /**
     * Adds a consumer to be notified when logging messages are received from the
     * server. This allows the client to react to log messages, such as warnings or
     * errors, that are sent by the server.
     *
     * @param loggingConsumer A consumer that receives logging messages. Must not be null.
     * @return This builder instance for method chaining
     */
    public SyncSpec loggingConsumer(Consumer<LoggingMessageNotification> loggingConsumer) {
        Objects.requireNonNull(loggingConsumer, "Logging consumer must not be null");
        loggingConsumers.add(loggingConsumer);

        return this;
    }

    /**
     * Adds multiple consumers to be notified when logging messages are received from
     * the server. This allows the client to react to log messages, such as warnings
     * or errors, that are sent by the server.
     *
     * @param loggingConsumers A list of consumers that receive logging messages. Must
     *                         not be null.
     * @return This builder instance for method chaining
     */
    public SyncSpec loggingConsumers(List<Consumer<LoggingMessageNotification>> loggingConsumers) {
        Objects.requireNonNull(loggingConsumers, "Logging consumers must not be null");
        this.loggingConsumers.addAll(loggingConsumers);

        return this;
    }

    /**
     * Create an instance of {@link McpSyncClient} with the provided configurations or
     * sensible defaults.
     *
     * @return a new instance of {@link McpSyncClient}.
     */
    public McpSyncClient build() {
        ClientSync syncFeatures = new ClientSync(clientInfo, capabilities,
                roots, toolsChangeConsumers, resourcesChangeConsumers, promptsChangeConsumers,
                loggingConsumers, samplingHandler);

        ClientAsync asyncFeatures = ClientAsync.fromSync(syncFeatures);

        return new McpSyncClient(new McpAsyncClient(transport, this.requestTimeout, this.initializationTimeout, asyncFeatures));
    }

}