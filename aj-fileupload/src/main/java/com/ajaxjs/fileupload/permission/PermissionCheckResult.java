package com.ajaxjs.fileupload.permission;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * POSIX 执行权限扫描结果。
 * <p>结果当前由同包扫描器填充并输出日志，不作为公共 DTO 暴露。</p>
 */
@Slf4j
public class PermissionCheckResult {
    /**
     * 发现的可执行普通文件。
     */
    Set<Path> executableFiles = new HashSet<>();

    /**
     * 发现的具有执行权限的目录。
     */
    Set<Path> executableDirs = new HashSet<>();

    void addExecutableFile(Path file) {
        executableFiles.add(file);
    }

    void addExecutableDir(Path dir) {
        executableDirs.add(dir);
    }

    void printSummary() {
        if (executableFiles.isEmpty() && executableDirs.isEmpty())
            log.info("✅ 所有文件和目录均无执行权限。");
        else {
            log.warn("❌ 发现可执行项：");

            for (Path dir : executableDirs)
                log.warn("\uD83D\uDCC1 可执行目录: {}", dir);

            for (Path file : executableFiles)
                log.warn("\uD83D\uDCC4 可执行文件: {}", file);
        }

        log.warn("共发现 {} 个可执行目录，{} 个可执行文件。", executableDirs.size(), executableFiles.size());
    }
}
