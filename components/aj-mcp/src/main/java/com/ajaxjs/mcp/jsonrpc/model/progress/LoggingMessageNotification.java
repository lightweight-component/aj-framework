package com.ajaxjs.mcp.jsonrpc.model.progress;

import lombok.Data;

/**
 * The Model Context Protocol (MCP) provides a standardized way for servers to send
 * structured log messages to clients. Clients can control logging verbosity by
 * setting minimum log levels, with servers sending notifications containing severity
 * levels, optional logger names, and arbitrary JSON-serializable data.
 */
@Data
public class LoggingMessageNotification {
    /**
     * The severity levels. The minimum log level is set by the client.
     */
    LoggingLevel level;

    /**
     * The logger that generated the message.
     */
    String logger;

    /**
     * JSON-serializable logging data.
     */
    String data;
}
