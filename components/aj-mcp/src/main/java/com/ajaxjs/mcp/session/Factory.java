package com.ajaxjs.mcp.session;

import com.ajaxjs.mcp.transport.McpServerTransport;

/**
 * Factory for creating server sessions which delegate to a provided 1:1 transport
 * with a connected client.
 */
@FunctionalInterface
public interface Factory {
    /**
     * Creates a new 1:1 representation of the client-server interaction.
     *
     * @param sessionTransport the transport to use for communication with the client.
     * @return a new server session.
     */
    McpServerSession create(McpServerTransport sessionTransport);
}