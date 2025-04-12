package com.ajaxjs.mcp.server;

import com.ajaxjs.mcp.McpConstant;
import com.ajaxjs.mcp.jsonrpc.model.Implementation;
import com.ajaxjs.mcp.jsonrpc.model.ListRootsResult;
import com.ajaxjs.mcp.jsonrpc.model.capabilities.ClientCapabilities;
import com.ajaxjs.mcp.jsonrpc.model.pagination.PaginatedRequest;
import com.ajaxjs.mcp.jsonrpc.model.sampling.CreateMessageRequest;
import com.ajaxjs.mcp.jsonrpc.model.sampling.CreateMessageResult;
import com.ajaxjs.mcp.model.McpError;
import com.ajaxjs.mcp.session.McpServerSession;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;

import java.util.concurrent.CompletableFuture;

/**
 * Represents an asynchronous exchange with a Model Context Protocol (MCP) client. The
 * exchange provides methods to interact with the client and query its capabilities.
 */
@Data
public class McpAsyncServerExchange {

    private final McpServerSession session;

    private final ClientCapabilities clientCapabilities;

    private final Implementation clientInfo;

    private static final TypeReference<CreateMessageResult> CREATE_MESSAGE_RESULT_TYPE_REF = new TypeReference<CreateMessageResult>() {
    };

    private static final TypeReference<ListRootsResult> LIST_ROOTS_RESULT_TYPE_REF = new TypeReference<ListRootsResult>() {
    };

    /**
     * Create a new asynchronous exchange with the client.
     *
     * @param session            The server session representing a 1-1 interaction.
     * @param clientCapabilities The client capabilities that define the supported features and functionality.
     * @param clientInfo         The client implementation information.
     */
    public McpAsyncServerExchange(McpServerSession session, ClientCapabilities clientCapabilities, Implementation clientInfo) {
        this.session = session;
        this.clientCapabilities = clientCapabilities;
        this.clientInfo = clientInfo;
    }

    /**
     * Get the client capabilities that define the supported features and functionality.
     *
     * @return The client capabilities
     */
    public ClientCapabilities getClientCapabilities() {
        return this.clientCapabilities;
    }

    /**
     * Get the client implementation information.
     *
     * @return The client implementation details
     */
    public Implementation getClientInfo() {
        return this.clientInfo;
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
     * @return A Mono that completes when the message has been created
     * @see CreateMessageRequest
     * @see CreateMessageResult
     * @see <a href=
     * "https://spec.modelcontextprotocol.io/specification/client/sampling/">Sampling
     * Specification</a>
     */
    public CompletableFuture<CreateMessageResult> createMessage(CreateMessageRequest createMessageRequest) {
        CompletableFuture<CreateMessageResult> future = new CompletableFuture<>();

        if (clientCapabilities == null) {
            future.completeExceptionally(new McpError("Client must be initialized. Call the initialize method first!"));
            return future;
        }

        if (clientCapabilities.getSampling() == null) {
            future.completeExceptionally(new McpError("Client must be configured with sampling capabilities"));
            return future;
        }

        return session.sendRequest(McpConstant.METHOD_SAMPLING_CREATE_MESSAGE, createMessageRequest, CREATE_MESSAGE_RESULT_TYPE_REF);
    }

    /**
     * Retrieves the list of all roots provided by the client.
     *
     * @return A Mono that emits the list of roots result.
     */
    public CompletableFuture<ListRootsResult> listRoots() {
        return listRoots(null);
    }

    /**
     * Retrieves a paginated list of roots provided by the client.
     *
     * @param cursor Optional pagination cursor from a previous list request
     * @return A Mono that emits the list of roots result containing
     */
    public CompletableFuture<ListRootsResult> listRoots(String cursor) {
        return this.session.sendRequest(McpConstant.METHOD_ROOTS_LIST, new PaginatedRequest(cursor), LIST_ROOTS_RESULT_TYPE_REF);
    }

}
