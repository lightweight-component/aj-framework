package com.ajaxjs.mcp.jsonrpc.model.resources;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 *
 */
@Data
@AllArgsConstructor
public class ListResourcesResult {
    List<Resource> resources;

    String nextCursor;
}
