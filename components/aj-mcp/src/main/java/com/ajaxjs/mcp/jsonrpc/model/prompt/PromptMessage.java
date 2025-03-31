package com.ajaxjs.mcp.jsonrpc.model.prompt;

import com.ajaxjs.mcp.jsonrpc.model.Role;
import com.ajaxjs.mcp.jsonrpc.model.content.Content;
import lombok.Data;

/**
 * Describes a message returned as part of a prompt.
 * <p>
 * This is similar to `SamplingMessage`, but also supports the embedding of resources from the MCP server.
 */
@Data
public class PromptMessage {
    /**
     * The sender or recipient of messages and data in a conversation.
     */
    Role role;

    /**
     * The content of the message of type {@link Content}.
     */
    Content content;
}
