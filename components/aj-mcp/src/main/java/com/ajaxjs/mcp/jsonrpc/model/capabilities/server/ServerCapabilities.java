package com.ajaxjs.mcp.jsonrpc.model.capabilities.server;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ServerCapabilities {
    Map<String, Object> experimental;

    LoggingCapabilities logging;

    PromptCapabilities prompts;

    ResourceCapabilities resources;

    ToolCapabilities tools;
}
