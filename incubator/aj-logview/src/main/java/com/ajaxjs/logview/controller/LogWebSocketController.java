package com.ajaxjs.logview.controller;

import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket 控制器
 * 处理客户端的 WebSocket 消息
 */
@Slf4j
@Controller
public class LogWebSocketController {
    @Autowired
    private LogMonitorService logMonitorService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 开始监控日志文件
     *
     * @param message 包含文件名的消息
     * @return 响应消息
     */
    @MessageMapping("/start-monitor")
    @SendTo("/topic/log-monitor")
    public Map<String, Object> startMonitor(Map<String, String> message) {
        String fileName = message.get("fileName");

        if (fileName == null || fileName.trim().isEmpty())
            return ObjectHelper.mapOf("type", "error", "message", "文件名不能为空");

        log.info("收到开始监控请求: {}", fileName);
        logMonitorService.startMonitoring(fileName);

        return ObjectHelper.mapOf("type", "monitor_started", "fileName", fileName, "message", "开始监控日志文件: " + fileName
        );
    }

    /**
     * 停止监控日志文件
     *
     * @param message 消息
     * @return 响应消息
     */
    @MessageMapping("/stop-monitor")
    @SendTo("/topic/log-monitor")
    public Map<String, Object> stopMonitor(Map<String, String> message) {
        log.info("收到停止监控请求");
        logMonitorService.stopMonitoring();

        return ObjectHelper.mapOf("type", "monitor_stopped", "message", "已停止日志监控");
    }

    /**
     * 获取监控状态
     *
     * @param message 消息
     * @return 监控状态
     */
    @MessageMapping("/monitor-status")
    @SendTo("/topic/log-monitor")
    public Map<String, Object> getMonitorStatus(Map<String, String> message) {
        Map<String, Object> status = logMonitorService.getMonitorStatus();
        status.put("type", "monitor_status");

        return status;
    }

    /**
     * 心跳检测
     *
     * @param message 心跳消息
     * @return 心跳响应
     */
    @MessageMapping("/heartbeat")
    @SendTo("/topic/log-monitor")
    public Map<String, Object> heartbeat(Map<String, String> message) {
        return ObjectHelper.mapOf("type", "heartbeat", "timestamp", System.currentTimeMillis(), "message", "pong");
    }
}