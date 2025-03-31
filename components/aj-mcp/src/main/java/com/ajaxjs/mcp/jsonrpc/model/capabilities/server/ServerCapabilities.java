package com.ajaxjs.mcp.jsonrpc.model.capabilities.server;

import lombok.Data;

import java.util.Map;

@Data
public class ServerCapabilities {
    Map<String, Object> experimental;

    LoggingCapabilities logging;

    PromptCapabilities prompts;

    ResourceCapabilities resources;

    ToolCapabilities tools;
}
