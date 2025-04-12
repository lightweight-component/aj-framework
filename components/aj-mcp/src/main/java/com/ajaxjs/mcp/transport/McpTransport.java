package com.ajaxjs.mcp.transport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ajaxjs.mcp.jsonrpc.schema.JSONRPCMessage;

import java.util.concurrent.CompletableFuture;

/**
 * 定义模型上下文协议 (MCP) 的异步传输层。
 *
 * <p>
 * McpTransport 接口为在模型上下文协议中实现自定义传输机制提供了基础。它处理客户端和服务器组件之间的双向通信，支持使用 JSON-RPC 格式的异步消息交换。
 * </p>
 *
 * <p>
 * 该接口的实现负责：
 * </p>
 * <ul>
 * <li>管理传输连接的生命周期</li>
 * <li>处理来自服务器的传入消息和错误</li>
 * <li>向服务器发送出站消息</li>
 * </ul>
 *
 * <p>
 * 传输层被设计为协议无关的，允许各种实现，如 WebSocket、HTTP 或自定义协议。
 * </p>
 */
public interface McpTransport {
    /**
     * 关闭传输连接并释放任何关联的资源。
     *
     * <p>
     * 当传输不再需要时，此方法确保资源的正确清理。它应处理任何活动连接的平稳关闭。
     * </p>
     */
    default void close() {
//        closeGracefully().subscribe();
        closeGracefully();
    }

    /**
     * 异步关闭传输连接并释放任何关联的资源。当连接已关闭时完成的。
     */
    CompletableFuture<Void> closeGracefully();

    /**
     * 异步向对端发送消息。当消息已发送时完成的 。
     *
     * <p>
     * 此方法以异步方式处理向服务器发送消息。消息以 MCP 协议规定的 JSON-RPC 格式发送。
     * </p>
     *
     * @param message 要发送到服务器的 {@link JSONRPCMessage}
     */
    CompletableFuture<Void> sendMessage(JSONRPCMessage message);

    /**
     * 将给定数据解组为指定类型的对象。
     *
     * @param <T>     要解组的对象类型
     * @param data    要解组的数据
     * @param typeRef 解组对象的类型引用
     * @return 解组后的对象
     */
    <T> T unmarshalFrom(Object data, TypeReference<T> typeRef);

}