package com.ajaxjs.mcp.jsonrpc.model.autocomplete;

import lombok.Data;

@Data
public class PromptReference implements PromptOrResourceReference {
    String type;

    String name;
}
