package com.ajaxjs.mcp.jsonrpc.model.content;

import com.ajaxjs.mcp.jsonrpc.model.Role;
import com.ajaxjs.mcp.jsonrpc.model.resources.ResourceContents;

import java.util.List;

public class EmbeddedResource implements Content {
    List<Role> audience;

    Double priority;

    ResourceContents resource;
}
