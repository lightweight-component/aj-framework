package com.ajaxjs.mcp.jsonrpc.model.resources;

import lombok.Data;

/**
 * Binary contents of a resource.
 */
@Data
public class BlobResourceContents implements ResourceContents {

    /**
     * the URI of this resource.
     */
    String uri;

    /**
     * the MIME type of this resource.
     */
    String mimeType;

    /**
     * a base64-encoded string representing the binary data of the resource. This must only be set if the resource can actually be represented as binary data (not text).
     */
    String blob;
}
