package com.ajaxjs.mcp.server.features;

import com.ajaxjs.mcp.jsonrpc.model.prompt.Prompt;
import com.ajaxjs.mcp.server.McpSyncServerExchange;
import com.ajaxjs.mcp.jsonrpc.model.prompt.GetPromptRequest;
import com.ajaxjs.mcp.jsonrpc.model.prompt.GetPromptResult;
import lombok.Data;

import java.util.function.BiFunction;

/**
 * Specification of a prompt template with its synchronous handler function. Prompts
 * provide structured templates for AI model interactions, supporting:
 * <ul>
 * <li>Consistent message formatting
 * <li>Parameter substitution
 * <li>Context injection
 * <li>Response formatting
 * <li>Instruction templating
 * </ul>
 *
 * <p>
 * Example prompt specification: <pre>{@code
 * new McpServerFeatures.SyncPromptSpecification(
 *     new Prompt("analyze", "Code analysis template"),
 *     (exchange, request) -> {
 *         String code = request.getArguments().get("code");
 *         return new GetPromptResult(
 *             "Analyze this code:\n\n" + code + "\n\nProvide feedback on:"
 *         );
 *     }
 * )
 * }</pre>
 */
@Data
public class SyncPromptSpecification {
    /**
     * The prompt definition including name and description
     */
    Prompt prompt;

    /**
     * The function that processes prompt requests and returns
     * formatted templates. The function's first argument is an
     * {@link McpSyncServerExchange} upon which the server can interact with the connected
     * client. The second arguments is a {@linkGetPromptRequest}.
     */
    BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> promptHandler;
}
