package com.ajaxjs.mcp.server.transport;

import com.ajaxjs.mcp.jsonrpc.schema.JSONRPCMessage;
import com.ajaxjs.mcp.transport.McpServerTransport;
import com.ajaxjs.mcp.utils.McpUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Implementation of McpServerTransport for the stdio session.
 */
@Slf4j
public class StdioMcpSessionTransport implements McpServerTransport {
    private final Sinks.Many<JSONRPCMessage> inboundSink;

    private final Sinks.Many<JSONRPCMessage> outboundSink;

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * Scheduler for handling inbound messages
     */
    private Scheduler inboundScheduler;

    /**
     * Scheduler for handling outbound messages
     */
    private Scheduler outboundScheduler;

    private final Sinks.One<Void> outboundReady = Sinks.one();

    public StdioMcpSessionTransport() {
        inboundSink = Sinks.many().unicast().onBackpressureBuffer();
        outboundSink = Sinks.many().unicast().onBackpressureBuffer();

        // Use bounded schedulers for better resource management
        inboundScheduler = Schedulers.fromExecutorService(Executors.newSingleThreadExecutor(), "stdio-inbound");
        outboundScheduler = Schedulers.fromExecutorService(Executors.newSingleThreadExecutor(), "stdio-outbound");
    }

    @Override
    public CompletableFuture<Void> sendMessage(JSONRPCMessage message) {
        return Mono.zip(inboundReady.asMono(), outboundReady.asMono()).then(Mono.defer(() -> {
            if (outboundSink.tryEmitNext(message).isSuccess()) {
                return CompletableFuture.completedFuture(null);
            } else {
                return McpUtils.error("Failed to enqueue message");
            }
        }));
    }

    @Override
    public <T> T unmarshalFrom(Object data, TypeReference<T> typeRef) {
        return objectMapper.convertValue(data, typeRef);
    }

    @Override
    public CompletableFuture<Void> closeGracefully() {
        return Mono.fromRunnable(() -> {
            isClosing.set(true);
            log.debug("Session transport closing gracefully");
            inboundSink.tryEmitComplete();
        });
    }

    @Override
    public void close() {
        isClosing.set(true);
        log.debug("Session transport closed");
    }

    private void initProcessing() {
        handleIncomingMessages();
        startInboundProcessing();
        startOutboundProcessing();
    }

    private void handleIncomingMessages() {
        this.inboundSink.asFlux().flatMap(message -> session.handle(message)).doOnTerminate(() -> {
            // The outbound processing will dispose its scheduler upon completion
            this.outboundSink.tryEmitComplete();
            this.inboundScheduler.dispose();
        }).subscribe();
    }

    /**
     * Starts the inbound processing thread that reads JSON-RPC messages from stdin.
     * Messages are deserialized and passed to the session for handling.
     */
    private void startInboundProcessing() {
        if (isStarted.compareAndSet(false, true)) {
            this.inboundScheduler.schedule(() -> {
                inboundReady.tryEmitValue(null);
                BufferedReader reader;

                try {
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    while (!isClosing.get()) {
                        try {
                            String line = reader.readLine();
                            if (line == null || isClosing.get()) {
                                break;
                            }

                            log.debug("Received JSON message: {}", line);

                            try {
                                McpSchema.JSONRPCMessage message = McpSchema.deserializeJsonRpcMessage(objectMapper, line);
                                if (!this.inboundSink.tryEmitNext(message).isSuccess()) {
                                    // logIfNotClosing("Failed to enqueue message");
                                    break;
                                }

                            } catch (Exception e) {
                                logIfNotClosing("Error processing inbound message", e);
                                break;
                            }
                        } catch (IOException e) {
                            logIfNotClosing("Error reading from stdin", e);
                            break;
                        }
                    }
                } catch (Exception e) {
                    logIfNotClosing("Error in inbound processing", e);
                } finally {
                    isClosing.set(true);
                    if (session != null)
                        session.close();

                    inboundSink.tryEmitComplete();
                }
            });
        }
    }

    /**
     * Starts the outbound processing thread that writes JSON-RPC messages to stdout.
     * Messages are serialized to JSON and written with a newline delimiter.
     */
    private void startOutboundProcessing() {
        Function<Flux<JSONRPCMessage>, Flux<JSONRPCMessage>> outboundConsumer = messages -> messages // @formatter:off
                .doOnSubscribe(subscription -> outboundReady.tryEmitValue(null))
                .publishOn(outboundScheduler)
                .handle((message, sink) -> {
                    if (message != null && !isClosing.get()) {
                        try {
                            String jsonMessage = objectMapper.writeValueAsString(message);
                            // Escape any embedded newlines in the JSON message as per spec
                            jsonMessage = jsonMessage.replace("\r\n", "\\n").replace("\n", "\\n").replace("\r", "\\n");

                            synchronized (outputStream) {
                                outputStream.write(jsonMessage.getBytes(StandardCharsets.UTF_8));
                                outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
                                outputStream.flush();
                            }
                            sink.next(message);
                        }
                        catch (IOException e) {
                            if (!isClosing.get()) {
                                log.error("Error writing message", e);
                                sink.error(new RuntimeException(e));
                            }
                            else {
                                log.debug("Stream closed during shutdown", e);
                            }
                        }
                    }
                    else if (isClosing.get()) {
                        sink.complete();
                    }
                })
                .doOnComplete(() -> {
                    isClosing.set(true);
                    outboundScheduler.dispose();
                })
                .doOnError(e -> {
                    if (!isClosing.get()) {
                        log.error("Error in outbound processing", e);
                        isClosing.set(true);
                        outboundScheduler.dispose();
                    }
                })
                .map(msg -> (JSONRPCMessage) msg);

        outboundConsumer.apply(outboundSink.asFlux()).subscribe();
    } // @formatter:on

    private void logIfNotClosing(String message, Exception e) {
        if (!isClosing.get()) {
            log.error(message, e);
        }
    }

}