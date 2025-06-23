package com.ajaxjs.mcp.jsonrpc.model.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginatedRequest {
    String cursor;
}
