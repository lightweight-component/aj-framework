package com.ajaxjs.logview.controller;

import com.ajaxjs.logview.LogParser;
import com.ajaxjs.logview.config.LogConfig;
import com.ajaxjs.logview.model.LogQueryRequest;
import com.ajaxjs.logview.model.LogQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 日志控制器
 */
@RestController
@RequestMapping("/api/logs")
@Slf4j
public class LogController {
    @Autowired
    private LogConfig logConfig;

    /**
     * 获取日志文件列表
     */
    @GetMapping("/files")
    public List<Map<String, Object>> getLogFiles() {
        File logDir = new File(logConfig.getLogPath());
        if (!logDir.exists() || !logDir.isDirectory())
            return Collections.emptyList();

        return Arrays.stream(Objects.requireNonNull(logDir.listFiles()))
                .filter(this::isValidLogFile)
                .map(this::fileToMap)
                .sorted((a, b) -> ((Long) b.get("lastModified")).compareTo((Long) a.get("lastModified")))
                .collect(Collectors.toList());
    }

    private Map<String, Object> fileToMap(File file) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", file.getName());
        map.put("size", file.length());
        map.put("lastModified", file.lastModified());
        map.put("readable", file.canRead());

        return map;
    }

    /**
     * 查询日志内容
     */
    @PostMapping("/query")
    public LogQueryResponse queryLogs(@RequestBody LogQueryRequest request) {
        File logFile = getLogFile(request.getFileName());
        validateFile(logFile);

        List<String> allLines = LogParser.readLines(logFile, StandardCharsets.UTF_8);
        List<String> filteredLines = filterLines(allLines, request);  // 过滤日志行

        if (request.isReverse())  // 倒序处理
            Collections.reverse(filteredLines);

        // 分页处理
        int totalLines = filteredLines.size();
        int totalPages = (int) Math.ceil((double) totalLines / request.getPageSize());
        int startIndex = (request.getPage() - 1) * request.getPageSize();
        int endIndex = Math.min(startIndex + request.getPageSize(), totalLines);

        List<String> pageLines = filteredLines.subList(startIndex, endIndex);

        LogQueryResponse response = new LogQueryResponse();
        response.setLines(pageLines);
        response.setTotalLines(totalLines);
        response.setCurrentPage(request.getPage());
        response.setTotalPages(totalPages);
        response.setFileSize(logFile.length());
        response.setLastModified(LocalDateTime.ofInstant(Instant.ofEpochMilli(logFile.lastModified()), ZoneId.systemDefault()));

        return response;
    }

    private File getLogFile(String fileName) {
        // 安全检查：防止路径遍历攻击
        if (logConfig.isEnableSecurity()) {
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\"))
                throw new IllegalArgumentException("非法的文件名");
        }

        return new File(logConfig.getLogPath(), fileName);
    }

    private void validateFile(File file) {
        if (!file.exists())
            throw new IllegalArgumentException("文件不存在");

        if (!file.isFile())
            throw new IllegalArgumentException("不是有效的文件");

        if (!isValidLogFile(file))
            throw new IllegalArgumentException("不支持的文件类型");

        long fileSizeMB = file.length() / (1024 * 1024);

        if (fileSizeMB > logConfig.getMaxFileSize())
            throw new IllegalArgumentException(String.format("文件过大，超过限制 %dMB", logConfig.getMaxFileSize()));
    }

    private static boolean hasFilter(LogQueryRequest request) {
        return StringUtils.hasText(request.getKeyword()) || StringUtils.hasText(request.getLevel()) ||
                request.getStartTime() != null ||
                request.getEndTime() != null;
    }

    private List<String> filterLines(List<String> lines, LogQueryRequest request) {
        if (!hasFilter(request))
            return lines;

        return lines.stream()
                .map(LogParser::parseLine)
                .filter(lineInfo -> lineInfo.matchesFilter(request))
                .map(LogParser.LogLineInfo::getOriginalLine)
                .collect(Collectors.toList());
    }

    private boolean isValidLogFile(File file) {
        String fileName = file.getName().toLowerCase();

        return logConfig.getAllowedExtensions().stream().anyMatch(fileName::endsWith);
    }

    /**
     * 下载日志文件
     */
    @GetMapping("/download/{fileName}")
    public void downloadLog(
            @PathVariable String fileName,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            HttpServletResponse response) throws IOException {
        LogQueryRequest request = new LogQueryRequest();
        request.setFileName(fileName);
        request.setKeyword(keyword);
        request.setLevel(level);
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        File logFile = getLogFile(fileName);
        validateFile(logFile);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

        if (hasFilter(request)) {
            // 下载过滤后的内容
            List<String> allLines = LogParser.readLines(logFile, StandardCharsets.UTF_8);
            List<String> filteredLines = filterLines(allLines, request);

            try (PrintWriter writer = response.getWriter()) {
                for (String line : filteredLines)
                    writer.println(line);
            }
        } else {
            // 下载原文件
            response.setContentLengthLong(logFile.length());
            try (InputStream inputStream = Files.newInputStream(logFile.toPath());
                 OutputStream outputStream = response.getOutputStream()) {
                StreamUtils.copy(inputStream, outputStream);
            }
        }
    }
}