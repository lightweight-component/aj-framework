package com.ajaxjs.logview.controller;

import com.ajaxjs.logview.LogParser;
import com.ajaxjs.logview.config.LogConfig;
import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 日志实时监控服务
 * 监控日志文件变化，实时推送新增日志内容
 */
@Slf4j
@Service
public class LogMonitorService implements InitializingBean, DisposableBean {
    @Autowired
    private LogConfig logConfig;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 文件监控服务
    private WatchService watchService;

    // 线程池
    private ScheduledExecutorService executorService;

    // 存储每个文件的读取位置
    private final Map<String, Long> filePositions = new ConcurrentHashMap<>();

    // 当前监控的文件
    private volatile String currentMonitorFile;

    // 监控状态
    private volatile boolean monitoring = false;

    @Override
    public void afterPropertiesSet() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            executorService = Executors.newScheduledThreadPool(2);
            Path logPath = Paths.get(logConfig.getLogPath());   // 注册日志目录监控

            if (Files.exists(logPath)) {
                logPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
                executorService.submit(this::watchFiles);  // 启动文件监控线程
                log.info("日志文件监控服务已启动，监控目录: {}", logPath);
            }
        } catch (Exception e) {
            log.error("初始化日志监控服务失败", e);
        }
    }

    @Override
    public void destroy() {
        monitoring = false;

        if (watchService != null) {
            try {
                watchService.close();
            } catch (Exception e) {
                log.error("关闭文件监控服务失败", e);
            }
        }

        if (executorService != null)
            executorService.shutdown();

        log.info("日志监控服务已关闭");
    }

    /**
     * 开始监控指定文件
     *
     * @param fileName 文件名
     */
    public void startMonitoring(String fileName) {
        if (fileName == null || fileName.trim().isEmpty())
            return;

        currentMonitorFile = fileName;
        monitoring = true;

        // 初始化文件读取位置
        File file = new File(logConfig.getLogPath(), fileName);
        if (file.exists())
            filePositions.put(fileName, file.length());

        log.info("开始监控日志文件: {}", fileName);

        // 发送监控开始消息
        messagingTemplate.convertAndSend("/topic/log-monitor", ObjectHelper.mapOf("type", "monitor_started", "fileName", fileName));
    }

    /**
     * 停止监控
     */
    public void stopMonitoring() {
        monitoring = false;
        currentMonitorFile = null;
        log.info("停止日志文件监控");

        // 发送监控停止消息
        messagingTemplate.convertAndSend("/topic/log-monitor", ObjectHelper.mapOf("type", "monitor_stopped"));
    }

    /**
     * 文件监控线程
     */
    private void watchFiles() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW)
                        continue;

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();

                    if (monitoring && fileName.toString().equals(currentMonitorFile))
                        // 延迟处理，避免文件正在写入
                        executorService.schedule(() -> processFileChange(currentMonitorFile), 100, TimeUnit.MILLISECONDS);
                }

                boolean valid = key.reset();

                if (!valid)
                    break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("文件监控异常", e);
            }
        }
    }

    /**
     * 处理文件变化
     *
     * @param fileName 文件名
     */
    private void processFileChange(String fileName) {
        File file = new File(logConfig.getLogPath(), fileName);
        if (!file.exists())
            return;

        long currentLength = file.length();
        long lastPosition = filePositions.getOrDefault(fileName, 0L);

        if (currentLength < lastPosition) // 如果文件被截断（如日志轮转），重置位置
            lastPosition = 0L;

        if (currentLength > lastPosition) {// 如果有新内容
            String newContent = readNewContent(file, lastPosition, currentLength);

            if (newContent != null && !newContent.trim().isEmpty()) {
                String[] lines = newContent.split("\n");  // 解析新日志行
                for (String line : lines) {
                    if (!line.trim().isEmpty())
                        sendLogLine(fileName, line);
                }

                filePositions.put(fileName, currentLength); // 更新文件位置
            }
        }
    }

    /**
     * 读取文件新增内容
     *
     * @param file          文件
     * @param startPosition 开始位置
     * @param endPosition   结束位置
     * @return 新增内容
     */
    private String readNewContent(File file, long startPosition, long endPosition) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(startPosition);

            long length = endPosition - startPosition;
            if (length > 1024 * 1024)  // 限制单次读取大小为1MB
                length = 1024 * 1024;

            byte[] buffer = new byte[(int) length];
            int bytesRead = raf.read(buffer);

            if (bytesRead > 0)
                return new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取文件内容失败: {}", file.getName(), e);
        }

        return null;
    }

    /**
     * 发送日志行到WebSocket客户端
     *
     * @param fileName 文件名
     * @param logLine  日志行
     */
    private void sendLogLine(String fileName, String logLine) {
        LogParser.LogLineInfo lineInfo = LogParser.parseLine(logLine);  // 解析日志行

        // 构建消息
        Map<String, Object> message = new HashMap<>();
        message.put("type", "new_log_line");
        message.put("fileName", fileName);
        message.put("content", logLine);
        message.put("timestamp", lineInfo.getTimestamp() != null ? lineInfo.getTimestamp().toString() : "");
        message.put("level", lineInfo.getLevel() != null ? lineInfo.getLevel() : "");
        message.put("rawContent", lineInfo.getContent() != null ? lineInfo.getContent() : logLine);

        messagingTemplate.convertAndSend("/topic/log-monitor", message); // 发送到WebSocket客户端
    }

    /**
     * 获取当前监控状态
     *
     * @return 监控状态信息
     */
    public Map<String, Object> getMonitorStatus() {
        return ObjectHelper.mapOf("monitoring", monitoring,
                "currentFile", currentMonitorFile != null ? currentMonitorFile : "",
                "monitoredFiles", filePositions.size()
        );
    }
}