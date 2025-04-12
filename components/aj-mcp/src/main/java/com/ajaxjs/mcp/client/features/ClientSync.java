package com.ajaxjs.mcp.client.features;

import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.Root;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.ClientCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.RootCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.progress.LoggingMessageNotification;
import com.ajaxjs.mcp.jsonrpc.model.prompt.Prompt;
import com.ajaxjs.mcp.jsonrpc.model.resources.Resource;
import com.ajaxjs.mcp.jsonrpc.model.sampling.CreateMessageRequest;
import com.ajaxjs.mcp.jsonrpc.model.sampling.CreateMessageResult;
import com.ajaxjs.mcp.jsonrpc.model.sampling.Sampling;
import com.ajaxjs.mcp.jsonrpc.model.tool.Tool;
import lombok.Data;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Synchronous client features specification providing the capabilities and request and notification handlers.
 */
@Data
public class ClientSync {
    /**
     * The client implementation information.
     */
    Implementation clientInfo;

    /**
     * The client capabilities.
     */
    ClientCapabilities clientCapabilities;

    /**
     * The roots.
     */
    Map<String, Root> roots;

    /**
     * The tools change consumers.
     */
    List<Consumer<List<Tool>>> toolsChangeConsumers;

    /**
     * The resources change consumers.
     */
    List<Consumer<List<Resource>>> resourcesChangeConsumers;

    /**
     * The prompts change consumers.
     */
    List<Consumer<List<Prompt>>> promptsChangeConsumers;

    /**
     * The logging consumers.
     */
    List<Consumer<LoggingMessageNotification>> loggingConsumers;

    /**
     * The sampling handler.
     */
    Function<CreateMessageRequest, CreateMessageResult> samplingHandler;

    /**
     * Create an instance and validate the arguments.
     *
     * @param clientInfo               the client implementation information.
     * @param clientCapabilities       the client capabilities.
     * @param roots                    the roots.
     * @param toolsChangeConsumers     the tools change consumers.
     * @param resourcesChangeConsumers the resources change consumers.
     * @param promptsChangeConsumers   the prompts change consumers.
     * @param loggingConsumers         the logging consumers.
     * @param samplingHandler          the sampling handler.
     */
    public ClientSync(Implementation clientInfo, ClientCapabilities clientCapabilities,
                      Map<String, Root> roots, List<Consumer<List<Tool>>> toolsChangeConsumers,
                      List<Consumer<List<Resource>>> resourcesChangeConsumers,
                      List<Consumer<List<Prompt>>> promptsChangeConsumers,
                      List<Consumer<LoggingMessageNotification>> loggingConsumers,
                      Function<CreateMessageRequest, CreateMessageResult> samplingHandler) {
        Objects.requireNonNull(clientInfo, "Client info must not be null");

        this.clientInfo = clientInfo;
        this.clientCapabilities = (clientCapabilities != null) ? clientCapabilities
                : new ClientCapabilities(null,
                !(roots == null || roots.isEmpty()) ? new RootCapabilities(false) : null,
                samplingHandler != null ? new Sampling() : null);
        this.roots = roots != null ? new HashMap<>(roots) : new HashMap<>();

        this.toolsChangeConsumers = toolsChangeConsumers != null ? toolsChangeConsumers : Collections.emptyList();
        this.resourcesChangeConsumers = resourcesChangeConsumers != null ? resourcesChangeConsumers : Collections.emptyList();
        this.promptsChangeConsumers = promptsChangeConsumers != null ? promptsChangeConsumers : Collections.emptyList();
        this.loggingConsumers = loggingConsumers != null ? loggingConsumers : Collections.emptyList();
        this.samplingHandler = samplingHandler;
    }
}
