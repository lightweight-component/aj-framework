package com.ajaxjs.mcp.jsonrpc.model.prompt;


import lombok.Data;

/**
 * Describes an argument that a prompt can accept.
 */
@Data
public class PromptArgument {
    /**
     * The name of the argument.
     */
    String name;

    /**
     * A human-readable description of the argument.
     */
    String description;

    /**
     * Whether this argument must be provided.
     */
    Boolean required;
}
