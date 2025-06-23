package com.ajaxjs.mcp.jsonrpc.model.prompt;

import com.ajaxjs.mcp.jsonrpc.schema.Request;
import lombok.Data;

import java.util.Map;

/**
 * Used by the client to get a prompt provided by the server.
 */
@Data
public class GetPromptRequest implements Request {
    /**
     * The name of the prompt or prompt template.
     */
    String name;

    /**
     * Arguments to use for templating the prompt.
     */
    Map<String, Object> arguments;
}
