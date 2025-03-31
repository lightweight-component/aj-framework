package com.ajaxjs.mcp.jsonrpc.model.tool;


import com.ajaxjs.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a tool that the server provides. Tools enable servers to expose
 * executable functionality to the system. Through these tools, you can interact with
 * external systems, perform computations, and take actions in the real world.
 */
@Data
public class Tool {
    /**
     * A unique identifier for the tool. This name is used when calling the tool.
     */
    String name;

    /**
     * A human-readable description of what the tool does. This can be used by clients to improve the LLM's understanding of available tools.
     */
    String description;

    /**
     * A JSON Schema object that describes the expected structure of
     * the arguments when calling this tool. This allows clients to validate tool
     * arguments before sending them to the server.
     */
    JsonSchema inputSchema;

    public Tool(String name, String description, String schema) {
        this.name = name;
        this.description = description;
        inputSchema = JsonUtil.fromJson(schema, JsonSchema.class);
    }
}
