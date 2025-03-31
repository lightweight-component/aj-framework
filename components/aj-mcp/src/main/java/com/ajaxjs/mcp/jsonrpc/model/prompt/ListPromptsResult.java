package com.ajaxjs.mcp.jsonrpc.model.prompt;

import java.util.List;


/**
 * The server's response to a prompts/list request from the client.
 */
public class ListPromptsResult {
    /**
     * A list of prompts that the server provides.
     */
    List<Prompt> prompts;

    /**
     * An optional cursor for pagination. If present, indicates there are more prompts available.
     */
    String nextCursor;
}
