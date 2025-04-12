package com.ajaxjs.mcp.jsonrpc.model.autocomplete;

import com.ajaxjs.mcp.jsonrpc.schema.Request;
import lombok.Data;

@Data
public class CompleteRequest implements Request {
    PromptOrResourceReference ref;

    CompleteArgument argument;
}
