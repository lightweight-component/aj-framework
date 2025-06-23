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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Asynchronous client features specification providing the capabilities and request and notification handlers.
 */
@Data
public class ClientAsync {
    Implementation clientInfo;
    ClientCapabilities clientCapabilities;
    Map<String, Root> roots;
    List<Function<List<Tool>, CompletableFuture<Void>>> toolsChangeConsumers;
    List<Function<java.util.List<Resource>, CompletableFuture<Void>>> resourcesChangeConsumers;
    List<Function<List<Prompt>, CompletableFuture<Void>>> promptsChangeConsumers;
    List<Function<LoggingMessageNotification, CompletableFuture<Void>>> loggingConsumers;
    Function<CreateMessageRequest, CompletableFuture<CreateMessageResult>> samplingHandler;

    public ClientAsync(Implementation clientInfo, ClientCapabilities clientCapabilities,
                       Map<String, Root> roots,
                       List<Function<List<Tool>, CompletableFuture<Void>>> toolsChangeConsumers,
                       List<Function<List<Resource>, CompletableFuture<Void>>> resourcesChangeConsumers,
                       List<Function<List<Prompt>, CompletableFuture<Void>>> promptsChangeConsumers,
                       List<Function<LoggingMessageNotification, CompletableFuture<Void>>> loggingConsumers,
                       Function<CreateMessageRequest, CompletableFuture<CreateMessageResult>> samplingHandler) {
        Objects.requireNonNull(clientInfo, "Client info must not be null");
        this.clientInfo = clientInfo;
        this.clientCapabilities = (clientCapabilities != null) ? clientCapabilities
                : new ClientCapabilities(null,
                !(roots == null || roots.isEmpty()) ? new RootCapabilities(false) : null,
                samplingHandler != null ? new Sampling() : null);
        this.roots = roots != null ? new ConcurrentHashMap<>(roots) : new ConcurrentHashMap<>();

        this.toolsChangeConsumers = toolsChangeConsumers != null ? toolsChangeConsumers : Collections.emptyList();
        this.resourcesChangeConsumers = resourcesChangeConsumers != null ? resourcesChangeConsumers : Collections.emptyList();
        this.promptsChangeConsumers = promptsChangeConsumers != null ? promptsChangeConsumers : Collections.emptyList();
        this.loggingConsumers = loggingConsumers != null ? loggingConsumers : Collections.emptyList();
        this.samplingHandler = samplingHandler;
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
    public static ClientAsync fromSync(ClientSync syncSpec) {
        List<Function<List<Tool>, CompletableFuture<Void>>> toolsChangeConsumers = new ArrayList<>();
        for (Consumer<List<Tool>> consumer : syncSpec.getToolsChangeConsumers()) {
            toolsChangeConsumers.add(t -> CompletableFuture.<Void>fromRunnable(() -> consumer.accept(t))
                    .subscribeOn(Schedulers.boundedElastic()));
        }

        List<Function<List<Resource>, CompletableFuture<Void>>> resourcesChangeConsumers = new ArrayList<>();
        for (Consumer<List<Resource>> consumer : syncSpec.getResourcesChangeConsumers()) {
            resourcesChangeConsumers.add(r -> CompletableFuture.<Void>fromRunnable(() -> consumer.accept(r))
                    .subscribeOn(Schedulers.boundedElastic()));
        }

        List<Function<List<Prompt>, CompletableFuture<Void>>> promptsChangeConsumers = new ArrayList<>();

        for (Consumer<List<Prompt>> consumer : syncSpec.getPromptsChangeConsumers()) {
            promptsChangeConsumers.add(p -> CompletableFuture.<Void>fromRunnable(() -> consumer.accept(p))
                    .subscribeOn(Schedulers.boundedElastic()));
        }

        List<Function<LoggingMessageNotification, CompletableFuture<Void>>> loggingConsumers = new ArrayList<>();
        for (Consumer<LoggingMessageNotification> consumer : syncSpec.getLoggingConsumers()) {
            loggingConsumers.add(l -> CompletableFuture.<Void>fromRunnable(() -> consumer.accept(l)).subscribeOn(Schedulers.boundedElastic()));
        }

        Function<CreateMessageRequest, CompletableFuture<CreateMessageResult>> samplingHandler = r -> CompletableFuture
                .fromCallable(() -> syncSpec.samplingHandler().apply(r))
                .subscribeOn(Schedulers.boundedElastic());

        return new ClientAsync(syncSpec.getClientInfo(), syncSpec.getClientCapabilities(), syncSpec.getRoots(),
                toolsChangeConsumers, resourcesChangeConsumers, promptsChangeConsumers, loggingConsumers,
                samplingHandler);
    }
}
