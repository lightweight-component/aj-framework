package com.ajaxjs.mcp.server.features;

import com.ajaxjs.mcp.jsonrpc.model.tool.CallToolResult;
import com.ajaxjs.mcp.jsonrpc.model.tool.Tool;
import com.ajaxjs.mcp.server.McpSyncServerExchange;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Specification of a tool with its synchronous handler function. Tools are the
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
 * new McpServerFeatures.SyncToolSpecification(
 *     new Tool(
 *         "calculator",
 *         "Performs mathematical calculations",
 *         new JsonSchemaObject()
 *             .required("expression")
 *             .property("expression", JsonSchemaType.STRING)
 *     ),
 *     (exchange, args) -> {
 *         String expr = (String) args.get("expression");
 *         return new CallToolResult("Result: " + evaluate(expr));
 *     }
 * )
 * }</pre>
 */
@Data
@AllArgsConstructor
public class SyncToolSpecification {
    /**
     * The tool definition including name, description, and parameter schema
     */
    Tool tool;

    /**
     * The function that implements the tool's logic, receiving arguments and
     * returning results. The function's first argument is an
     * {@link McpSyncServerExchange} upon which the server can interact with the connected
     * client. The second arguments is a map of arguments passed to the tool.
     */
    BiFunction<McpSyncServerExchange, Map<String, Object>, CallToolResult> call;
}
