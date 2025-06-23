package com.ajaxjs.mcp.jsonrpc.model.prompt;

import lombok.Data;

import java.util.List;

/**
 * A prompt or prompt template that the server offers.
 */
@Data
public class Prompt {
    /**
     * The name of the prompt or prompt template.
     */
    String name;

    /**
     * An optional description of what this prompt provides.
     */
    String description;

    /**
     * A list of arguments to use for templating the prompt.
     */
    List<PromptArgument> arguments;
}
