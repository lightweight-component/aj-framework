package com.ajaxjs.mcp.jsonrpc.model;

import lombok.Data;

/**
 * 表示服务器可以操作的根目录或文件。
 */
@Data
public class Root {
    /**
     * 标识根目录的 URI。目前，该 URI 必须以 file:// 开头。
     * 此限制可能会在协议的未来版本中放宽，以支持其他 URI 方案。
     */
    private String uri;

    /**
     * 根目录的可选名称。此名称可用于提供根目录的人类可读标识符，
     * 可能对显示目的或在应用程序的其他部分引用根目录时有用。
     */
    private String name;
}


