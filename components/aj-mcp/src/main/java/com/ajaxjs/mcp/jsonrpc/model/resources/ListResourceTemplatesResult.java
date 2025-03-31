package com.ajaxjs.mcp.jsonrpc.model.resources;

import lombok.Data;

import java.util.List;

@Data
public class ListResourceTemplatesResult {
    List<ResourceTemplate> resourceTemplates;

    String nextCursor;
}
