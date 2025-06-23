/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.ajaxjs.mcp.server;

import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.Root;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.server.ServerCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.resources.ResourceTemplate;
import com.ajaxjs.mcp.jsonrpc.model.tool.CallToolResult;
import com.ajaxjs.mcp.jsonrpc.model.tool.Tool;
import com.ajaxjs.mcp.server.asyncserver.McpAsyncServer;
import com.ajaxjs.mcp.server.features.*;
import com.ajaxjs.mcp.transport.McpServerTransportProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Factory class for creating Model Context Protocol (MCP) servers. MCP servers expose
 * tools, resources, and prompts to AI models through a standardized interface.
 *
 * <p>
 * This class serves as the main entry point for implementing the server-side of the MCP
 * specification. The server's responsibilities include:
 * <ul>
 * <li>Exposing tools that models can invoke to perform actions
 * <li>Providing access to resources that give models context
 * <li>Managing prompt templates for structured model interactions
 * <li>Handling client connections and requests
 * <li>Implementing capability negotiation
 * </ul>
 *
 * <p>
 * Thread Safety: Both synchronous and asynchronous server implementations are
 * thread-safe. The synchronous server processes requests sequentially, while the
 * asynchronous server can handle concurrent requests safely through its reactive
 * programming model.
 *
 * <p>
 * Error Handling: The server implementations provide robust error handling through the
 * McpError class. Errors are properly propagated to clients while maintaining the
 * server's stability. Server implementations should use appropriate error codes and
 * provide meaningful error messages to help diagnose issues.
 *
 * <p>
 * The class provides factory methods to create either:
 * <ul>
 * <li>{@link McpAsyncServer} for non-blocking operations with reactive responses
 * <li>{@link McpSyncServer} for blocking operations with direct responses
 * </ul>
 *
 * <p>
 * Example of creating a basic synchronous server: <pre>{@code
 * McpServer.sync(transportProvider)
 *     .serverInfo("my-server", "1.0.0")
 *     .tool(new Tool("calculator", "Performs calculations", schema),
 *           (exchange, args) -> new CallToolResult("Result: " + calculate(args)))
 *     .build();
 * }</pre>
 * <p>
 * Example of creating a basic asynchronous server: <pre>{@code
 * McpServer.async(transportProvider)
 *     .serverInfo("my-server", "1.0.0")
 *     .tool(new Tool("calculator", "Performs calculations", schema),
 *           (exchange, args) -> Mono.fromSupplier(() -> calculate(args))
 *               .map(result -> new CallToolResult("Result: " + result)))
 *     .build();
 * }</pre>
 *
 * <p>
 * Example with comprehensive asynchronous configuration: <pre>{@code
 * McpServer.async(transportProvider)
 *     .serverInfo("advanced-server", "2.0.0")
 *     .capabilities(new ServerCapabilities(...))
 *     // Register tools
 *     .tools(
 *         new AsyncToolSpecification(calculatorTool,
 *             (exchange, args) -> Mono.fromSupplier(() -> calculate(args))
 *                 .map(result -> new CallToolResult("Result: " + result))),
 *         new AsyncToolSpecification(weatherTool,
 *             (exchange, args) -> Mono.fromSupplier(() -> getWeather(args))
 *                 .map(result -> new CallToolResult("Weather: " + result)))
 *     )
 *     // Register resources
 *     .resources(
 *         new AsyncResourceSpecification(fileResource,
 *             (exchange, req) -> Mono.fromSupplier(() -> readFile(req))
 *                 .map(ReadResourceResult::new)),
 *         new AsyncResourceSpecification(dbResource,
 *             (exchange, req) -> Mono.fromSupplier(() -> queryDb(req))
 *                 .map(ReadResourceResult::new))
 *     )
 *     // Add resource templates
 *     .resourceTemplates(
 *         new ResourceTemplate("file://{path}", "Access files"),
 *         new ResourceTemplate("db://{table}", "Access database")
 *     )
 *     // Register prompts
 *     .prompts(
 *         new AsyncPromptSpecification(analysisPrompt,
 *             (exchange, req) -> Mono.fromSupplier(() -> generateAnalysisPrompt(req))
 *                 .map(GetPromptResult::new)),
 *         new AsyncPromptRegistration(summaryPrompt,
 *             (exchange, req) -> Mono.fromSupplier(() -> generateSummaryPrompt(req))
 *                 .map(GetPromptResult::new))
 *     )
 *     .build();
 * }</pre>
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 * @see McpAsyncServer
 * @see McpSyncServer
 * @see McpServerTransportProvider
 */
public interface McpServer {
    /**
     * Starts building a synchronous MCP server that provides blocking operations.
     * Synchronous servers block the current Thread's execution upon each request before
     * giving the control back to the caller, making them simpler to implement but
     * potentially less scalable for concurrent operations.
     *
     * @param transportProvider The transport layer implementation for MCP communication.
     * @return A new instance of {@link SyncSpecification} for configuring the server.
     */
    static SyncSpecification sync(McpServerTransportProvider transportProvider) {
        return new SyncSpecification(transportProvider);
    }

    /**
     * Starts building an asynchronous MCP server that provides non-blocking operations.
     * Asynchronous servers can handle multiple requests concurrently on a single Thread
     * using a functional paradigm with non-blocking server transports, making them more
     * scalable for high-concurrency scenarios but more complex to implement.
     *
     * @param transportProvider The transport layer implementation for MCP communication.
     * @return A new instance of {@link AsyncSpecification} for configuring the server.
     */
    static AsyncSpecification async(McpServerTransportProvider transportProvider) {
        return new AsyncSpecification(transportProvider);
    }

    /**
     * Asynchronous server specification.
     */
    class AsyncSpecification {

        private static final Implementation DEFAULT_SERVER_INFO = new Implementation("mcp-server", "1.0.0");

        private final McpServerTransportProvider transportProvider;

        private ObjectMapper objectMapper;

        private Implementation serverInfo = DEFAULT_SERVER_INFO;

        private ServerCapabilities serverCapabilities;

        /**
         * The Model Context Protocol (MCP) allows servers to expose tools that can be
         * invoked by language models. Tools enable models to interact with external
         * systems, such as querying databases, calling APIs, or performing computations.
         * Each tool is uniquely identified by a name and includes metadata describing its
         * schema.
         */
        private final List<AsyncToolSpecification> tools = new ArrayList<>();

        /**
         * The Model Context Protocol (MCP) provides a standardized way for servers to
         * expose resources to clients. Resources allow servers to share data that
         * provides context to language models, such as files, database schemas, or
         * application-specific information. Each resource is uniquely identified by a
         * URI.
         */
        private final Map<String, AsyncResourceSpecification> resources = new HashMap<>();

        private final List<ResourceTemplate> resourceTemplates = new ArrayList<>();

        /**
         * The Model Context Protocol (MCP) provides a standardized way for servers to
         * expose prompt templates to clients. Prompts allow servers to provide structured
         * messages and instructions for interacting with language models. Clients can
         * discover available prompts, retrieve their contents, and provide arguments to
         * customize them.
         */
        private final Map<String, AsyncPromptSpecification> prompts = new HashMap<>();

        private final List<BiFunction<McpAsyncServerExchange, List<Root>, CompletableFuture<Void>>> rootsChangeHandlers = new ArrayList<>();

        private AsyncSpecification(McpServerTransportProvider transportProvider) {
            Objects.requireNonNull(transportProvider, "Transport provider must not be null");
            this.transportProvider = transportProvider;
        }

        /**
         * Sets the server implementation information that will be shared with clients
         * during connection initialization. This helps with version compatibility,
         * debugging, and server identification.
         *
         * @param serverInfo The server implementation details including name and version.
         *                   Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if serverInfo is null
         */
        public AsyncSpecification serverInfo(Implementation serverInfo) {
            Objects.requireNonNull(serverInfo, "Server info must not be null");
            this.serverInfo = serverInfo;
            return this;
        }

        /**
         * Sets the server implementation information using name and version strings. This
         * is a convenience method alternative to
         * {@link #serverInfo(Implementation)}.
         *
         * @param name    The server name. Must not be null or empty.
         * @param version The server version. Must not be null or empty.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if name or version is null or empty
         * @see #serverInfo(Implementation)
         */
        public AsyncSpecification serverInfo(String name, String version) {
            Objects.requireNonNull(name, "Name must not be null or empty");
            Objects.requireNonNull(version, "Version must not be null or empty");
            this.serverInfo = new Implementation(name, version);

            return this;
        }

        /**
         * Sets the server capabilities that will be advertised to clients during
         * connection initialization. Capabilities define what features the server
         * supports, such as:
         * <ul>
         * <li>Tool execution
         * <li>Resource access
         * <li>Prompt handling
         * </ul>
         *
         * @param serverCapabilities The server capabilities configuration. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if serverCapabilities is null
         */
        public AsyncSpecification capabilities(ServerCapabilities serverCapabilities) {
            Objects.requireNonNull(serverCapabilities, "Server capabilities must not be null");
            this.serverCapabilities = serverCapabilities;

            return this;
        }

        /**
         * Adds a single tool with its implementation handler to the server. This is a
         * convenience method for registering individual tools without creating a
         * {@link AsyncToolSpecification} explicitly.
         *
         * <p>
         * Example usage: <pre>{@code
         * .tool(
         *     new Tool("calculator", "Performs calculations", schema),
         *     (exchange, args) -> Mono.fromSupplier(() -> calculate(args))
         *         .map(result -> new CallToolResult("Result: " + result))
         * )
         * }</pre>
         *
         * @param tool    The tool definition including name, description, and schema. Must
         *                not be null.
         * @param handler The function that implements the tool's logic. Must not be null.
         *                The function's first argument is an {@link McpAsyncServerExchange} upon which
         *                the server can interact with the connected client. The second argument is the
         *                map of arguments passed to the tool.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if tool or handler is null
         */
        public AsyncSpecification tool(Tool tool, BiFunction<McpAsyncServerExchange, Map<String, Object>, CompletableFuture<CallToolResult>> handler) {
            Objects.requireNonNull(tool, "Tool must not be null");
            Objects.requireNonNull(handler, "Handler must not be null");

            this.tools.add(new AsyncToolSpecification(tool, handler));

            return this;
        }

        /**
         * Adds multiple tools with their handlers to the server using a List. This method
         * is useful when tools are dynamically generated or loaded from a configuration
         * source.
         *
         * @param toolSpecifications The list of tool specifications to add. Must not be
         *                           null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if toolSpecifications is null
         * @see #tools(AsyncToolSpecification...)
         */
        public AsyncSpecification tools(List<AsyncToolSpecification> toolSpecifications) {
            Objects.requireNonNull(toolSpecifications, "Tool handlers list must not be null");
            this.tools.addAll(toolSpecifications);

            return this;
        }

        /**
         * Adds multiple tools with their handlers to the server using varargs. This
         * method provides a convenient way to register multiple tools inline.
         *
         * <p>
         * Example usage: <pre>{@code
         * .tools(
         *     new AsyncToolSpecification(calculatorTool, calculatorHandler),
         *     new AsyncToolSpecification(weatherTool, weatherHandler),
         *     new AsyncToolSpecification(fileManagerTool, fileManagerHandler)
         * )
         * }</pre>
         *
         * @param toolSpecifications The tool specifications to add. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if toolSpecifications is null
         * @see #tools(List)
         */
        public AsyncSpecification tools(AsyncToolSpecification... toolSpecifications) {
            Objects.requireNonNull(toolSpecifications, "Tool handlers list must not be null");
            for (AsyncToolSpecification tool : toolSpecifications) {
                this.tools.add(tool);
            }

            return this;
        }

        /**
         * Registers multiple resources with their handlers using a Map. This method is
         * useful when resources are dynamically generated or loaded from a configuration
         * source.
         *
         * @param resourceSpecifications Map of resource name to specification. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if resourceSpecifications is null
         * @see #resources(AsyncResourceSpecification...)
         */
        public AsyncSpecification resources(Map<String, AsyncResourceSpecification> resourceSpecifications) {
            Objects.requireNonNull(resourceSpecifications, "Resource handlers map must not be null");
            this.resources.putAll(resourceSpecifications);

            return this;
        }

        /**
         * Registers multiple resources with their handlers using a List. This method is
         * useful when resources need to be added in bulk from a collection.
         *
         * @param resourceSpecifications List of resource specifications. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if resourceSpecifications is null
         * @see #resources(AsyncResourceSpecification...)
         */
        public AsyncSpecification resources(List<AsyncResourceSpecification> resourceSpecifications) {
            Objects.requireNonNull(resourceSpecifications, "Resource handlers list must not be null");
            for (AsyncResourceSpecification resource : resourceSpecifications)
                resources.put(resource.getResource().getUri(), resource);

            return this;
        }

        /**
         * Registers multiple resources with their handlers using varargs. This method
         * provides a convenient way to register multiple resources inline.
         *
         * <p>
         * Example usage: <pre>{@code
         * .resources(
         *     new AsyncResourceSpecification(fileResource, fileHandler),
         *     new AsyncResourceSpecification(dbResource, dbHandler),
         *     new AsyncResourceSpecification(apiResource, apiHandler)
         * )
         * }</pre>
         *
         * @param resourceSpecifications The resource specifications to add. Must not be
         *                               null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if resourceSpecifications is null
         */
        public AsyncSpecification resources(AsyncResourceSpecification... resourceSpecifications) {
            Objects.requireNonNull(resourceSpecifications, "Resource handlers list must not be null");
            for (AsyncResourceSpecification resource : resourceSpecifications)
                this.resources.put(resource.getResource().getUri(), resource);

            return this;
        }

        /**
         * Sets the resource templates that define patterns for dynamic resource access.
         * Templates use URI patterns with placeholders that can be filled at runtime.
         *
         * <p>
         * Example usage: <pre>{@code
         * .resourceTemplates(
         *     new ResourceTemplate("file://{path}", "Access files by path"),
         *     new ResourceTemplate("db://{table}/{id}", "Access database records")
         * )
         * }</pre>
         *
         * @param resourceTemplates List of resource templates. If null, clears existing
         *                          templates.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if resourceTemplates is null.
         * @see #resourceTemplates(ResourceTemplate...)
         */
        public AsyncSpecification resourceTemplates(List<ResourceTemplate> resourceTemplates) {
            Objects.requireNonNull(resourceTemplates, "Resource templates must not be null");
            this.resourceTemplates.addAll(resourceTemplates);

            return this;
        }

        /**
         * Sets the resource templates using varargs for convenience. This is an
         * alternative to {@link #resourceTemplates(List)}.
         *
         * @param resourceTemplates The resource templates to set.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if resourceTemplates is null.
         * @see #resourceTemplates(List)
         */
        public AsyncSpecification resourceTemplates(ResourceTemplate... resourceTemplates) {
            Objects.requireNonNull(resourceTemplates, "Resource templates must not be null");
            for (ResourceTemplate resourceTemplate : resourceTemplates) {
                this.resourceTemplates.add(resourceTemplate);
            }

            return this;
        }

        /**
         * Registers multiple prompts with their handlers using a Map. This method is
         * useful when prompts are dynamically generated or loaded from a configuration
         * source.
         *
         * <p>
         * Example usage: <pre>{@code
         * .prompts(Map.of("analysis", new AsyncPromptSpecification(
         *     new Prompt("analysis", "Code analysis template"),
         *     request -> Mono.fromSupplier(() -> generateAnalysisPrompt(request))
         *         .map(GetPromptResult::new)
         * )));
         * }</pre>
         *
         * @param prompts Map of prompt name to specification. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if prompts is null
         */
        public AsyncSpecification prompts(Map<String, AsyncPromptSpecification> prompts) {
            Objects.requireNonNull(prompts, "Prompts map must not be null");
            this.prompts.putAll(prompts);

            return this;
        }

        /**
         * Registers multiple prompts with their handlers using a List. This method is
         * useful when prompts need to be added in bulk from a collection.
         *
         * @param prompts List of prompt specifications. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if prompts is null
         * @see #prompts(AsyncPromptSpecification...)
         */
        public AsyncSpecification prompts(List<AsyncPromptSpecification> prompts) {
            Objects.requireNonNull(prompts, "Prompts list must not be null");
            for (AsyncPromptSpecification prompt : prompts)
                this.prompts.put(prompt.getPrompt().getName(), prompt);

            return this;
        }

        /**
         * Registers multiple prompts with their handlers using varargs. This method
         * provides a convenient way to register multiple prompts inline.
         *
         * <p>
         * Example usage: <pre>{@code
         * .prompts(
         *     new AsyncPromptSpecification(analysisPrompt, analysisHandler),
         *     new AsyncPromptSpecification(summaryPrompt, summaryHandler),
         *     new AsyncPromptSpecification(reviewPrompt, reviewHandler)
         * )
         * }</pre>
         *
         * @param prompts The prompt specifications to add. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if prompts is null
         */
        public AsyncSpecification prompts(AsyncPromptSpecification... prompts) {
            Objects.requireNonNull(prompts, "Prompts list must not be null");
            for (AsyncPromptSpecification prompt : prompts) {
                this.prompts.put(prompt.getPrompt().getName(), prompt);
            }

            return this;
        }

        /**
         * Registers a consumer that will be notified when the list of roots changes. This
         * is useful for updating resource availability dynamically, such as when new
         * files are added or removed.
         *
         * @param handler The handler to register. Must not be null. The function's first
         *                argument is an {@link McpAsyncServerExchange} upon which the server can
         *                interact with the connected client. The second argument is the list of roots.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if consumer is null
         */
        public AsyncSpecification rootsChangeHandler(BiFunction<McpAsyncServerExchange, List<Root>, CompletableFuture<Void>> handler) {
            Objects.requireNonNull(handler, "Consumer must not be null");
            this.rootsChangeHandlers.add(handler);
            return this;
        }

        /**
         * Registers multiple consumers that will be notified when the list of roots
         * changes. This method is useful when multiple consumers need to be registered at
         * once.
         *
         * @param handlers The list of handlers to register. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if consumers is null
         * @see #rootsChangeHandler(BiFunction)
         */
        public AsyncSpecification rootsChangeHandlers(
                List<BiFunction<McpAsyncServerExchange, List<Root>, CompletableFuture<Void>>> handlers) {
            Objects.requireNonNull(handlers, "Handlers list must not be null");
            this.rootsChangeHandlers.addAll(handlers);

            return this;
        }

        /**
         * Registers multiple consumers that will be notified when the list of roots
         * changes using varargs. This method provides a convenient way to register
         * multiple consumers inline.
         *
         * @param handlers The handlers to register. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if consumers is null
         * @see #rootsChangeHandlers(List)
         */
        public AsyncSpecification rootsChangeHandlers(@SuppressWarnings("unchecked") BiFunction<McpAsyncServerExchange, List<Root>, CompletableFuture<Void>>... handlers) {
            Objects.requireNonNull(handlers, "Handlers list must not be null");
            return rootsChangeHandlers(Arrays.asList(handlers));
        }

        /**
         * Sets the object mapper to use for serializing and deserializing JSON messages.
         *
         * @param objectMapper the instance to use. Must not be null.
         * @return This builder instance for method chaining.
         * @throws IllegalArgumentException if objectMapper is null
         */
        public AsyncSpecification objectMapper(ObjectMapper objectMapper) {
            Objects.requireNonNull(objectMapper, "ObjectMapper must not be null");
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         * Builds an asynchronous MCP server that provides non-blocking operations.
         *
         * @return A new instance of {@link McpAsyncServer} configured with this builder's
         * settings.
         */
        public McpAsyncServer build() {
            ServerFeaturesAsync features = new ServerFeaturesAsync(this.serverInfo, this.serverCapabilities, this.tools,
                    this.resources, this.resourceTemplates, this.prompts, this.rootsChangeHandlers);

            return new McpAsyncServer(transportProvider, new ObjectMapper(), features);
        }

    }

    /**
     * Synchronous server specification.
     */
    class SyncSpecification {
        private static final Implementation DEFAULT_SERVER_INFO = new Implementation("mcp-server", "1.0.0");

        private final McpServerTransportProvider transportProvider;

        private ObjectMapper objectMapper;

        private Implementation serverInfo = DEFAULT_SERVER_INFO;

        private ServerCapabilities serverCapabilities;

        /**
         * The Model Context Protocol (MCP) allows servers to expose tools that can be
         * invoked by language models. Tools enable models to interact with external
         * systems, such as querying databases, calling APIs, or performing computations.
         * Each tool is uniquely identified by a name and includes metadata describing its
         * schema.
         */
        private final List<SyncToolSpecification> tools = new ArrayList<>();

        /**
         * The Model Context Protocol (MCP) provides a standardized way for servers to
         * expose resources to clients. Resources allow servers to share data that
         * provides context to language models, such as files, database schemas, or
         * application-specific information. Each resource is uniquely identified by a
         * URI.
         */
        private final Map<String, SyncResourceSpecification> resources = new HashMap<>();

        private final List<ResourceTemplate> resourceTemplates = new ArrayList<>();

        /**
         * The Model Context Protocol (MCP) provides a standardized way for servers to
         * expose prompt templates to clients. Prompts allow servers to provide structured
         * messages and instructions for interacting with language models. Clients can
         * discover available prompts, retrieve their contents, and provide arguments to
         * customize them.
         */
        private final Map<String, SyncPromptSpecification> prompts = new HashMap<>();

        private final List<BiConsumer<McpSyncServerExchange, List<Root>>> rootsChangeHandlers = new ArrayList<>();

        private SyncSpecification(McpServerTransportProvider transportProvider) {
            Objects.requireNonNull(transportProvider, "Transport provider must not be null");
            this.transportProvider = transportProvider;
        }

        /**
         * Sets the server implementation information that will be shared with clients
         * during connection initialization. This helps with version compatibility,
         * debugging, and server identification.
         *
         * @param serverInfo The server implementation details including name and version.
         *                   Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if serverInfo is null
         */
        public SyncSpecification serverInfo(Implementation serverInfo) {
            Objects.requireNonNull(serverInfo, "Server info must not be null");
            this.serverInfo = serverInfo;
            return this;
        }

        /**
         * Sets the server implementation information using name and version strings. This
         * is a convenience method alternative to
         * {@link #serverInfo(Implementation)}.
         *
         * @param name    The server name. Must not be null or empty.
         * @param version The server version. Must not be null or empty.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if name or version is null or empty
         * @see #serverInfo(Implementation)
         */
        public SyncSpecification serverInfo(String name, String version) {
            Objects.requireNonNull(name, "Name must not be null or empty");
            Objects.requireNonNull(version, "Version must not be null or empty");
            this.serverInfo = new Implementation(name, version);

            return this;
        }

        /**
         * Sets the server capabilities that will be advertised to clients during
         * connection initialization. Capabilities define what features the server
         * supports, such as:
         * <ul>
         * <li>Tool execution
         * <li>Resource access
         * <li>Prompt handling
         * </ul>
         *
         * @param serverCapabilities The server capabilities configuration. Must not be
         *                           null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if serverCapabilities is null
         */
        public SyncSpecification capabilities(ServerCapabilities serverCapabilities) {
            Objects.requireNonNull(serverCapabilities, "Server capabilities must not be null");
            this.serverCapabilities = serverCapabilities;
            return this;
        }

        /**
         * Adds a single tool with its implementation handler to the server. This is a
         * convenience method for registering individual tools without creating a
         * {@link SyncToolSpecification} explicitly.
         *
         * <p>
         * Example usage: <pre>{@code
         * .tool(
         *     new Tool("calculator", "Performs calculations", schema),
         *     (exchange, args) -> new CallToolResult("Result: " + calculate(args))
         * )
         * }</pre>
         *
         * @param tool    The tool definition including name, description, and schema. Must
         *                not be null.
         * @param handler The function that implements the tool's logic. Must not be null.
         *                The function's first argument is an {@link McpSyncServerExchange} upon which
         *                the server can interact with the connected client. The second argument is the
         *                list of arguments passed to the tool.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if tool or handler is null
         */
        public SyncSpecification tool(Tool tool, BiFunction<McpSyncServerExchange, Map<String, Object>, CallToolResult> handler) {
            Objects.requireNonNull(tool, "Tool must not be null");
            Objects.requireNonNull(handler, "Handler must not be null");

            this.tools.add(new SyncToolSpecification(tool, handler));

            return this;
        }

        /**
         * Adds multiple tools with their handlers to the server using a List. This method
         * is useful when tools are dynamically generated or loaded from a configuration
         * source.
         *
         * @param toolSpecifications The list of tool specifications to add. Must not be
         *                           null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if toolSpecifications is null
         * @see #tools(SyncToolSpecification...)
         */
        public SyncSpecification tools(List<SyncToolSpecification> toolSpecifications) {
            Objects.requireNonNull(toolSpecifications, "Tool handlers list must not be null");
            this.tools.addAll(toolSpecifications);
            return this;
        }

        /**
         * Adds multiple tools with their handlers to the server using varargs. This
         * method provides a convenient way to register multiple tools inline.
         *
         * <p>
         * Example usage: <pre>{@code
         * .tools(
         *     new ToolSpecification(calculatorTool, calculatorHandler),
         *     new ToolSpecification(weatherTool, weatherHandler),
         *     new ToolSpecification(fileManagerTool, fileManagerHandler)
         * )
         * }</pre>
         *
         * @param toolSpecifications The tool specifications to add. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if toolSpecifications is null
         * @see #tools(List)
         */
        public SyncSpecification tools(SyncToolSpecification... toolSpecifications) {
            Objects.requireNonNull(toolSpecifications, "Tool handlers list must not be null");

            for (SyncToolSpecification tool : toolSpecifications)
                tools.add(tool);

            return this;
        }

        /**
         * Registers multiple resources with their handlers using a Map. This method is
         * useful when resources are dynamically generated or loaded from a configuration
         * source.
         *
         * @param resourceSpecifications Map of resource name to specification. Must not
         *                               be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if resourceSpecifications is null
         * @see #resources(SyncResourceSpecification...)
         */
        public SyncSpecification resources(Map<String, SyncResourceSpecification> resourceSpecifications) {
            Objects.requireNonNull(resourceSpecifications, "Resource handlers map must not be null");
            this.resources.putAll(resourceSpecifications);
            return this;
        }

        /**
         * Registers multiple resources with their handlers using a List. This method is
         * useful when resources need to be added in bulk from a collection.
         *
         * @param resourceSpecifications List of resource specifications. Must not be
         *                               null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if resourceSpecifications is null
         * @see #resources(SyncResourceSpecification...)
         */
        public SyncSpecification resources(List<SyncResourceSpecification> resourceSpecifications) {
            Objects.requireNonNull(resourceSpecifications, "Resource handlers list must not be null");
            for (SyncResourceSpecification resource : resourceSpecifications) {
                this.resources.put(resource.getResource().getUri(), resource);
            }
            return this;
        }

        /**
         * Registers multiple resources with their handlers using varargs. This method
         * provides a convenient way to register multiple resources inline.
         *
         * <p>
         * Example usage: <pre>{@code
         * .resources(
         *     new ResourceSpecification(fileResource, fileHandler),
         *     new ResourceSpecification(dbResource, dbHandler),
         *     new ResourceSpecification(apiResource, apiHandler)
         * )
         * }</pre>
         *
         * @param resourceSpecifications The resource specifications to add. Must not be
         *                               null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if resourceSpecifications is null
         */
        public SyncSpecification resources(SyncResourceSpecification... resourceSpecifications) {
            Objects.requireNonNull(resourceSpecifications, "Resource handlers list must not be null");
            for (SyncResourceSpecification resource : resourceSpecifications) {
                this.resources.put(resource.getResource().getUri(), resource);
            }
            return this;
        }

        /**
         * Sets the resource templates that define patterns for dynamic resource access.
         * Templates use URI patterns with placeholders that can be filled at runtime.
         *
         * <p>
         * Example usage: <pre>{@code
         * .resourceTemplates(
         *     new ResourceTemplate("file://{path}", "Access files by path"),
         *     new ResourceTemplate("db://{table}/{id}", "Access database records")
         * )
         * }</pre>
         *
         * @param resourceTemplates List of resource templates. If null, clears existing
         *                          templates.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if resourceTemplates is null.
         * @see #resourceTemplates(ResourceTemplate...)
         */
        public SyncSpecification resourceTemplates(List<ResourceTemplate> resourceTemplates) {
            Objects.requireNonNull(resourceTemplates, "Resource templates must not be null");
            this.resourceTemplates.addAll(resourceTemplates);
            return this;
        }

        /**
         * Sets the resource templates using varargs for convenience. This is an
         * alternative to {@link #resourceTemplates(List)}.
         *
         * @param resourceTemplates The resource templates to set.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if resourceTemplates is null
         * @see #resourceTemplates(List)
         */
        public SyncSpecification resourceTemplates(ResourceTemplate... resourceTemplates) {
            Objects.requireNonNull(resourceTemplates, "Resource templates must not be null");
            for (ResourceTemplate resourceTemplate : resourceTemplates) {
                this.resourceTemplates.add(resourceTemplate);
            }
            return this;
        }

        /**
         * Registers multiple prompts with their handlers using a Map. This method is
         * useful when prompts are dynamically generated or loaded from a configuration
         * source.
         *
         * <p>
         * Example usage: <pre>{@code
         * Map<String, PromptSpecification> prompts = new HashMap<>();
         * prompts.put("analysis", new PromptSpecification(
         *     new Prompt("analysis", "Code analysis template"),
         *     (exchange, request) -> new GetPromptResult(generateAnalysisPrompt(request))
         * ));
         * .prompts(prompts)
         * }</pre>
         *
         * @param prompts Map of prompt name to specification. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if prompts is null
         */
        public SyncSpecification prompts(Map<String, SyncPromptSpecification> prompts) {
            Objects.requireNonNull(prompts, "Prompts map must not be null");
            this.prompts.putAll(prompts);
            return this;
        }

        /**
         * Registers multiple prompts with their handlers using a List. This method is
         * useful when prompts need to be added in bulk from a collection.
         *
         * @param prompts List of prompt specifications. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if prompts is null
         * @see #prompts(SyncPromptSpecification...)
         */
        public SyncSpecification prompts(List<SyncPromptSpecification> prompts) {
            Objects.requireNonNull(prompts, "Prompts list must not be null");
            for (SyncPromptSpecification prompt : prompts) {
                this.prompts.put(prompt.getPrompt().getName(), prompt);
            }
            return this;
        }

        /**
         * Registers multiple prompts with their handlers using varargs. This method
         * provides a convenient way to register multiple prompts inline.
         *
         * <p>
         * Example usage: <pre>{@code
         * .prompts(
         *     new PromptSpecification(analysisPrompt, analysisHandler),
         *     new PromptSpecification(summaryPrompt, summaryHandler),
         *     new PromptSpecification(reviewPrompt, reviewHandler)
         * )
         * }</pre>
         *
         * @param prompts The prompt specifications to add. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if prompts is null
         */
        public SyncSpecification prompts(SyncPromptSpecification... prompts) {
            Objects.requireNonNull(prompts, "Prompts list must not be null");
            for (SyncPromptSpecification prompt : prompts) {
                this.prompts.put(prompt.getPrompt().getName(), prompt);
            }
            return this;
        }

        /**
         * Registers a consumer that will be notified when the list of roots changes. This
         * is useful for updating resource availability dynamically, such as when new
         * files are added or removed.
         *
         * @param handler The handler to register. Must not be null. The function's first
         *                argument is an {@link McpSyncServerExchange} upon which the server can interact
         *                with the connected client. The second argument is the list of roots.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if consumer is null
         */
        public SyncSpecification rootsChangeHandler(BiConsumer<McpSyncServerExchange, List<Root>> handler) {
            Objects.requireNonNull(handler, "Consumer must not be null");
            this.rootsChangeHandlers.add(handler);
            return this;
        }

        /**
         * Registers multiple consumers that will be notified when the list of roots
         * changes. This method is useful when multiple consumers need to be registered at
         * once.
         *
         * @param handlers The list of handlers to register. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if consumers is null
         * @see #rootsChangeHandler(BiConsumer)
         */
        public SyncSpecification rootsChangeHandlers(List<BiConsumer<McpSyncServerExchange, List<Root>>> handlers) {
            Objects.requireNonNull(handlers, "Handlers list must not be null");
            this.rootsChangeHandlers.addAll(handlers);
            return this;
        }

        /**
         * Registers multiple consumers that will be notified when the list of roots
         * changes using varargs. This method provides a convenient way to register
         * multiple consumers inline.
         *
         * @param handlers The handlers to register. Must not be null.
         * @return This builder instance for method chaining
         * @throws IllegalArgumentException if consumers is null
         * @see #rootsChangeHandlers(List)
         */
        public SyncSpecification rootsChangeHandlers(BiConsumer<McpSyncServerExchange, List<Root>>... handlers) {
            Objects.requireNonNull(handlers, "Handlers list must not be null");

            return this.rootsChangeHandlers(Arrays.asList(handlers));
        }

        /**
         * Sets the object mapper to use for serializing and deserializing JSON messages.
         *
         * @param objectMapper the instance to use. Must not be null.
         * @return This builder instance for method chaining.
         * @throws IllegalArgumentException if objectMapper is null
         */
        public SyncSpecification objectMapper(ObjectMapper objectMapper) {
            Objects.requireNonNull(objectMapper, "ObjectMapper must not be null");
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         * Builds a synchronous MCP server that provides blocking operations.
         *
         * @return A new instance of {@link McpSyncServer} configured with this builder's
         * settings.
         */
        public McpSyncServer build() {
            ServerFeaturesSync syncFeatures = new ServerFeaturesSync(serverInfo, serverCapabilities, tools, resources, resourceTemplates, prompts, rootsChangeHandlers);
            ServerFeaturesAsync asyncFeatures = ServerFeaturesAsync.fromSync(syncFeatures);
            McpAsyncServer asyncServer = new McpAsyncServer(this.transportProvider, new ObjectMapper(), asyncFeatures);

            return new McpSyncServer(asyncServer);
        }

    }

}
