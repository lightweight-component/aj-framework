package com.ajaxjs.mcp.jsonrpc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 客户端对服务器发出的 roots/list 请求的响应结果。
 * 此结果包含一个 Root 对象数组，每个对象表示服务器可以操作的根目录或文件。
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListRootsResult {
    /**
     * 一个 Root 对象数组，每个对象表示服务器可以操作的根目录或文件。
     */
    private List<Root> roots;
}
