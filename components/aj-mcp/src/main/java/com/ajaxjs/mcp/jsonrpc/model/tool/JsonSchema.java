package com.ajaxjs.mcp.jsonrpc.model.tool;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class JsonSchema {
    String type;


    Map<String, Object> properties;


    List<String> required;

    Boolean additionalProperties;
}
