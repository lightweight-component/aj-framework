package com.ajaxjs.mcp.jsonrpc.model.capabilities;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Roots 定义了服务器可以在文件系统中操作的边界，使其了解可以访问哪些目录和文件。
 * 服务器可以从支持的客户端请求根目录列表，并在该列表发生变化时接收通知。
 */
@Data
@AllArgsConstructor
public class RootCapabilities {
    /**
     * 客户端是否会在根目录发生变化时发送通知
     */
    private Boolean listChanged;
}
