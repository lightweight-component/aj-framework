package com.ajaxjs.mcp.jsonrpc.model.resources;

import lombok.Data;

/**
 * A known resource that the server is capable of reading.
 */
@Data
public class Resource implements Annotated {
    /**
     * the URI of the resource.
     */
    String uri;

    /**
     * A human-readable name for this resource. This can be used by clients to populate UI elements.
     */
    String name;

    /**
     * A description of what this resource represents. This can be used by clients to improve the LLM's understanding of available resources. It can be thought of like a "hint" to the model.
     */
    String description;

    /**
     * The MIME type of this resource, if known.
     */
    String mimeType;

    /**
     * Optional annotations for the client. The client can use annotations to inform how objects are used or displayed.
     */
    Annotations annotations;
}
