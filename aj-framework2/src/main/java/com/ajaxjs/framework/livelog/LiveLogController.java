package com.ajaxjs.framework.livelog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@RestController
@Slf4j
@ConditionalOnProperty(name = "aj-framework.livelog.enable", havingValue = "true")
public class LiveLogController {
    /**
     * 存储所有活跃的 SSE 连接
     */
    private static final CopyOnWriteArraySet<SseEmitter> emitters = new CopyOnWriteArraySet<>();

    /**
     * 用于控制日志监控
     * 不管多少个连接，只有一个  LogFileTailer 实例
     */
    private volatile LogFileTailer tailer;

    private volatile boolean isMonitoring = false;

    @Value("${aj-framework.livelog.logFilePath}")
    private  String logFilePath;

    /**
     * SSE 端点 - 客户端连接后开始推送日志
     */
    @GetMapping(value = "/tomcat_log_stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // 设置超时时间为最大值

        Runnable cleanup = () -> { // 添加连接事件
            emitters.remove(emitter);
            if (emitters.isEmpty())
                stopLogMonitoring();
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError((ex) -> {
            log.error("Error in SSE connection: " + ex.getMessage(), ex);
            cleanup.run();
        });

        emitters.add(emitter);

        if (!isMonitoring)  // 如果还没有启动日志监控，则启动它
            startLogMonitoring();

        return emitter;
    }

    /**
     * 启动日志监控
     */
    private void startLogMonitoring() {
        if (!isMonitoring) {
            synchronized (this) {
                if (!isMonitoring) {
                    tailer = new LogFileTailer(logFilePath);
                    tailer.setTailing(true);
                    tailer.setListener(this::sendLogToAllClients);
                    tailer.start();
                    isMonitoring = true;
                }
            }
        }
    }

    /**
     * 停止日志监控
     */
    private void stopLogMonitoring() {
        if (isMonitoring) {
            synchronized (this) {
                if (isMonitoring && emitters.isEmpty()) {
                    if (tailer != null)
                        tailer.setTailing(false);

                    isMonitoring = false;
                }
            }
        }
    }

    /**
     * 向所有连接的客户端发送日志消息
     */
    private void sendLogToAllClients(String logMessage) {
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .data(logMessage + "<br/>")
                        .build());

                return false; // 不移除正常的连接
            } catch (IOException e) {
                log.error("Error sending log message to client: " + e.getMessage(), e);

                return true; // 连接已断开，需要移除
            }
        });
    }
}
