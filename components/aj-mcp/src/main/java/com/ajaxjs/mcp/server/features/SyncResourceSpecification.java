package com.ajaxjs.mcp.server.features;

import com.ajaxjs.mcp.jsonrpc.model.resources.ReadResourceRequest;
import com.ajaxjs.mcp.jsonrpc.model.resources.ReadResourceResult;
import com.ajaxjs.mcp.jsonrpc.model.resources.Resource;
import com.ajaxjs.mcp.server.McpSyncServerExchange;
import lombok.Data;

import java.util.function.BiFunction;

/**
 * Specification of a resource with its synchronous handler function. Resources
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
 * new McpServerFeatures.SyncResourceSpecification(
 *     new Resource("docs", "Documentation files", "text/markdown"),
 *     (exchange, request) -> {
 *         String content = readFile(request.getPath());
 *         return new ReadResourceResult(content);
 *     }
 * )
 * }</pre>
 *
 */
@Data
public class SyncResourceSpecification {
    /**
     * The resource definition including name, description, and MIME type
     */
    Resource resource;

    /**
     * The function that handles resource read requests. The function's
     * first argument is an {@link McpSyncServerExchange} upon which the server can
     * interact with the connected client. The second arguments is a
     * {@link ReadResourceRequest}.
     */
    BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> readHandler;
}
