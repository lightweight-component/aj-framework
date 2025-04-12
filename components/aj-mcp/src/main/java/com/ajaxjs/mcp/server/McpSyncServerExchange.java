package com.ajaxjs.mcp.server;

import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.ListRootsResult;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.ClientCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.sampling.CreateMessageRequest;
import com.ajaxjs.mcp.jsonrpc.model.sampling.CreateMessageResult;

/**
 * Represents a synchronous exchange with a Model Context Protocol (MCP) client. The
 * exchange provides methods to interact with the client and query its capabilities.
 */
public class McpSyncServerExchange {
    private final McpAsyncServerExchange exchange;

    /**
     * Create a new synchronous exchange with the client using the provided asynchronous
     * implementation as a delegate.
     *
     * @param exchange The asynchronous exchange to delegate to.
     */
    public McpSyncServerExchange(McpAsyncServerExchange exchange) {
        this.exchange = exchange;
    }

    /**
     * Get the client capabilities that define the supported features and functionality.
     *
     * @return The client capabilities
     */
    public ClientCapabilities getClientCapabilities() {
        return exchange.getClientCapabilities();
    }

    /**
     * Get the client implementation information.
     *
     * @return The client implementation details
     */
    public Implementation getClientInfo() {
        return exchange.getClientInfo();
    }

    /**
     * Create a new message using the sampling capabilities of the client. The Model
     * Context Protocol (MCP) provides a standardized way for servers to request LLM
     * sampling (“completions” or “generations”) from language models via clients. This
     * flow allows clients to maintain control over model access, selection, and
     * permissions while enabling servers to leverage AI capabilities—with no server API
     * keys necessary. Servers can request text or image-based interactions and optionally
     * include context from MCP servers in their prompts.
     *
     * @param createMessageRequest The request to create a new message
     * @return A result containing the details of the sampling response
     * @see CreateMessageRequest
     * @see CreateMessageResult
     * @see <a href=
     * "https://spec.modelcontextprotocol.io/specification/client/sampling/">Sampling
     * Specification</a>
     */
    public CreateMessageResult createMessage(CreateMessageRequest createMessageRequest) {
        return exchange.createMessage(createMessageRequest).join();
    }

    /**
     * Retrieves the list of all roots provided by the client.
     *
     * @return The list of roots result.
     */
    public ListRootsResult listRoots() {
        return exchange.listRoots().join();
    }

    /**
     * Retrieves a paginated list of roots provided by the client.
     *
     * @param cursor Optional pagination cursor from a previous list request
     * @return The list of roots result
     */
    public ListRootsResult listRoots(String cursor) {
        return exchange.listRoots(cursor).join();
    }

}
