package com.ajaxjs.mcp.jsonrpc.model.resources;

import lombok.Data;

/**
 * Text contents of a resource.
 */
@Data
public class TextResourceContents implements ResourceContents {
    /**
     * the URI of this resource.
     */
    String uri;

    /**
     * the MIME type of this resource.
     */
    String mimeType;

    /**
     * the text of the resource. This must only be set if the resource can actually be represented as text (not binary data).
     */
    String text;
}
