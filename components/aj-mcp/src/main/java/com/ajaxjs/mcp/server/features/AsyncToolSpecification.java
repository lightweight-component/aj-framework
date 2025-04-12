package com.ajaxjs.mcp.server.features;

import com.ajaxjs.mcp.jsonrpc.model.tool.CallToolResult;
import com.ajaxjs.mcp.jsonrpc.model.tool.Tool;
import com.ajaxjs.mcp.server.McpAsyncServerExchange;
import com.ajaxjs.mcp.server.McpSyncServerExchange;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;


/**
 * Specification of a tool with its asynchronous handler function. Tools are the
 * primary way for MCP servers to expose functionality to AI models. Each tool
 * represents a specific capability, such as:
 * <ul>
 * <li>Performing calculations
 * <li>Accessing external APIs
 * <li>Querying databases
 * <li>Manipulating files
 * <li>Executing system commands
 * </ul>
 *
 * <p>
 * Example tool specification: <pre>{@code
 * new McpServerFeatures.AsyncToolSpecification(
 *     new Tool(
 *         "calculator",
 *         "Performs mathematical calculations",
 *         new JsonSchemaObject()
 *             .required("expression")
 *             .property("expression", JsonSchemaType.STRING)
 *     ),
 *     (exchange, args) -> {
 *         String expr = (String) args.get("expression");
 *         return Mono.fromSupplier(() -> evaluate(expr))
 *             .map(result -> new CallToolResult("Result: " + result));
 *     }
 * )
 * }</pre>
 */
@Data
@AllArgsConstructor
public class AsyncToolSpecification {
    /**
     * The tool definition including name, description, and parameter schema
     */
    Tool tool;

    /**
     * The function that implements the tool's logic, receiving arguments and
     * returning results. The function's first argument is an
     * {@link McpAsyncServerExchange} upon which the server can interact with the
     * connected client. The second arguments is a map of tool arguments.
     */
    BiFunction<McpAsyncServerExchange, Map<String, Object>, CompletableFuture<CallToolResult>> call;

    public static AsyncToolSpecification fromSync(SyncToolSpecification tool) {
        // FIXME: This is temporary, proper validation should be implemented
        if (tool == null) {
            return null;
        }
        return new AsyncToolSpecification(tool.getTool(),
                (exchange, map) -> Mono
                        .fromCallable(() -> tool.getCall().apply(new McpSyncServerExchange(exchange), map))
                        .subscribeOn(Schedulers.boundedElastic()));
    }
}

