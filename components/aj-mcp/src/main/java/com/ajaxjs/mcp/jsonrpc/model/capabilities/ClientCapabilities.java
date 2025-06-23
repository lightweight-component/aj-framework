package com.ajaxjs.mcp.jsonrpc.model.capabilities;

import com.ajaxjs.mcp.jsonrpc.model.sampling.Sampling;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * 客户端可以实现额外的功能来为已连接的 MCP 服务器提供额外的能力。
 * 这些能力可以用来扩展服务器的功能，或者向服务器提供关于客户端能力的额外信息。
 */
@Data
@AllArgsConstructor
public class ClientCapabilities {
    /**
     * 实验性功能（开发中）
     */
    private Map<String, Object> experimental;

    /**
     * 定义服务器可以在文件系统中操作的边界，使其了解可以访问哪些目录和文件
     */
    private RootCapabilities roots;

    /**
     * 为服务器提供一种标准化的方式，通过客户端从语言模型请求 LLM 采样（"完成"或"生成"）
     */
    private Sampling sampling;
}
