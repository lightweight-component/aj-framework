package com.ajaxjs.mcp.jsonrpc.model.tool;

import com.ajaxjs.mcp.jsonrpc.model.content.Content;
import lombok.Data;

import java.util.List;

/**
 * The server's response to a tools/call request from the client.
 */
@Data
public class CallToolResult {
    /**
     * A list of content items representing the tool's output. Each item can be text, an image, or an embedded resource.
     */
    List<Content> content;

    /**
     * If true, indicates that the tool execution failed and the content contains error information.
     * If false or absent, indicates successful execution.
     */
    Boolean isError;
}
