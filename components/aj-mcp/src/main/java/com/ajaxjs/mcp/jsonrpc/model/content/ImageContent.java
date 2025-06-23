package com.ajaxjs.mcp.jsonrpc.model.content;

import com.ajaxjs.mcp.jsonrpc.model.Role;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ImageContent implements Content {
    List<Role> audience;

    Double priority;

    String data;
}
