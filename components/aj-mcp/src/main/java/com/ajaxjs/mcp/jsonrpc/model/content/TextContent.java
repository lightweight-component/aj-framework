package com.ajaxjs.mcp.jsonrpc.model.content;

import com.ajaxjs.mcp.jsonrpc.model.Role;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TextContent implements Content {
    List<Role> audience;

    Double priority;

    String text;

    public TextContent(String content) {
        this(null, null, content);
    }
}
