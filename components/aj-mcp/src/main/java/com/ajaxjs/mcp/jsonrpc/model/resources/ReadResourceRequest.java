package com.ajaxjs.mcp.jsonrpc.model.resources;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReadResourceRequest {
    String uri;
}
