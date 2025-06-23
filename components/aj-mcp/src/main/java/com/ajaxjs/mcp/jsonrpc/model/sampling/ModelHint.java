package com.ajaxjs.mcp.jsonrpc.model.sampling;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModelHint {
    String name;

    public static ModelHint of(String name) {
        return new ModelHint(name);
    }
}
