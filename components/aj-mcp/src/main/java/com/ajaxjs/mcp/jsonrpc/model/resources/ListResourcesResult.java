package com.ajaxjs.mcp.jsonrpc.model.resources;

import lombok.Data;

import java.util.List;

/**
 *
 */
@Data
public class ListResourcesResult {
    List<Resource> resources;

    String nextCursor;
}
