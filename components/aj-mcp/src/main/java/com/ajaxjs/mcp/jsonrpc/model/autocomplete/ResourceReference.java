package com.ajaxjs.mcp.jsonrpc.model.autocomplete;

import lombok.Data;

@Data
public class ResourceReference implements PromptOrResourceReference {
    String type;
    String uri;
}
