package com.ajaxjs.mcp.server.features;

import com.ajaxjs.mcp.jsonrpc.model.prompt.GetPromptRequest;
import com.ajaxjs.mcp.jsonrpc.model.prompt.GetPromptResult;
import com.ajaxjs.mcp.jsonrpc.model.prompt.Prompt;
import com.ajaxjs.mcp.server.McpAsyncServerExchange;
import com.ajaxjs.mcp.server.McpSyncServerExchange;
import lombok.Data;

import java.util.function.BiFunction;
import java.util.concurrent.CompletableFuture;

/**
 * Specification of a prompt template with its asynchronous handler function. Prompts
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
 * new McpServerFeatures.AsyncPromptSpecification(
 *     new Prompt("analyze", "Code analysis template"),
 *     (exchange, request) -> {
 *         String code = request.getArguments().get("code");
 *         return Mono.just(new GetPromptResult(
 *             "Analyze this code:\n\n" + code + "\n\nProvide feedback on:"
 *         ));
 *     }
 * )
 * }</pre>
 */
@Data
public class AsyncPromptSpecification {
    /**
     * The prompt definition including name and description
     */
    Prompt prompt;

    /**
     * The function that processes prompt requests and returns
     * formatted templates. The function's first argument is an
     * {@link McpAsyncServerExchange} upon which the server can interact with the
     * connected client. The second arguments is a
     * {@link GetPromptRequest}.
     */
    BiFunction<McpAsyncServerExchange, GetPromptRequest, CompletableFuture<GetPromptResult>> promptHandler;

    public static AsyncPromptSpecification fromSync(SyncPromptSpecification prompt) {
        // FIXME: This is temporary, proper validation should be implemented
        if (prompt == null)
            return null;

        return new AsyncPromptSpecification(prompt.getPrompt(), (exchange, req) -> Mono
                .fromCallable(() -> prompt.getPromptHandler().apply(new McpSyncServerExchange(exchange), req))
                .subscribeOn(Schedulers.boundedElastic()));
    }
}
