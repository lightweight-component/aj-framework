package com.ajaxjs.mcp.jsonrpc.model.content;

public interface Content {
    default String type() {
        if (this instanceof TextContent)
            return "text";
        else if (this instanceof ImageContent)
            return "image";
        else if (this instanceof EmbeddedResource)
            return "resource";

        throw new IllegalArgumentException("Unknown content type: " + this);
    }
}
