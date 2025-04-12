package com.ajaxjs.mcp.jsonrpc.model.autocomplete;

import lombok.Data;

import java.util.List;

@Data
public class CompleteCompletion {
    List<String> values;
    Integer total;
    Boolean hasMore;
}
