package com.ajaxjs.mcp.jsonrpc.model.prompt;

import java.util.List;

/**
 * The server's response to a prompts/get request from the client.
 */
public class GetPromptResult {
    /**
     * An optional description for the prompt.
     */
    String description;

    /**
     * A list of messages to display as part of the prompt.
     */
    List<PromptMessage> messages;
}
