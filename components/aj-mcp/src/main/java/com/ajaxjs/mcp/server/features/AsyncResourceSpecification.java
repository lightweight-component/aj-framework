package com.ajaxjs.mcp.server.features;

import com.ajaxjs.mcp.jsonrpc.model.resources.ReadResourceRequest;
import com.ajaxjs.mcp.jsonrpc.model.resources.ReadResourceResult;
import com.ajaxjs.mcp.jsonrpc.model.resources.Resource;
import com.ajaxjs.mcp.server.McpAsyncServerExchange;
import com.ajaxjs.mcp.server.McpSyncServerExchange;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Specification of a resource with its asynchronous handler function. Resources
 * provide context to AI models by exposing data such as:
 * <ul>
 * <li>File contents
 * <li>Database records
 * <li>API responses
 * <li>System information
 * <li>Application state
 * </ul>
 *
 * <p>
 * Example resource specification: <pre>{@code
 * new McpServerFeatures.AsyncResourceSpecification(
 *     new Resource("docs", "Documentation files", "text/markdown"),
 *     (exchange, request) ->
 *         Mono.fromSupplier(() -> readFile(request.getPath()))
 *             .map(ReadResourceResult::new)
 * )
 * }</pre>
 */
@Data
@AllArgsConstructor
public class AsyncResourceSpecification {
    /**
     * The resource definition including name, description, and MIME type
     */
    Resource resource;

    /**
     * The function that handles resource read requests. The function's
     * first argument is an {@link McpAsyncServerExchange} upon which the server can
     * interact with the connected client. The second arguments is a
     * {@link ReadResourceRequest}.
     */
    BiFunction<McpAsyncServerExchange, ReadResourceRequest, CompletableFuture<ReadResourceResult>> readHandler;

    public static AsyncResourceSpecification fromSync(SyncResourceSpecification resource) {
        // FIXME: This is temporary, proper validation should be implemented
        if (resource == null) {
            return null;
        }
        return new AsyncResourceSpecification(resource.getResource(),
                (exchange, req) -> Mono
                        .fromCallable(() -> resource.getReadHandler().apply(new McpSyncServerExchange(exchange), req))
                        .subscribeOn(Schedulers.boundedElastic()));
    }
}
