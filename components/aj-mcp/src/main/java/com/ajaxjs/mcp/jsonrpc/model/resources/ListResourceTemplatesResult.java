package com.ajaxjs.mcp.jsonrpc.model.resources;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ListResourceTemplatesResult {
    List<ResourceTemplate> resourceTemplates;

    String nextCursor;
}
