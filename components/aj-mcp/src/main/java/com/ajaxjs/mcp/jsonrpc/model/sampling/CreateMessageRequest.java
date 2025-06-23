package com.ajaxjs.mcp.jsonrpc.model.sampling;

import com.ajaxjs.mcp.jsonrpc.schema.Request;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Sampling and Message Creation
 */
@Data
public class CreateMessageRequest implements Request {
    List<SamplingMessage> messages;

    ModelPreferences modelPreferences;

    String systemPrompt;

    ContextInclusionStrategy includeContext;

    Double temperature;

    int maxTokens;

    List<String> stopSequences;

    Map<String, Object> metadata;
}
