package com.ajaxjs.fileupload.permission;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * POSIX execute permission scan result.
 * <p>The result is currently populated by the same-package scanner and logged, not exposed as a public DTO.</p>
 */
@Slf4j
public class PermissionCheckResult {
    /**
     * Executable regular files found.
     */
    Set<Path> executableFiles = new HashSet<>();

    /**
     * Directories with execute permission found.
     */
    Set<Path> executableDirs = new HashSet<>();

    /**
     * Adds a regular file that has execute permission to the result set.
     *
     * @param file the file path to record
     */
    void addExecutableFile(Path file) {
        executableFiles.add(file);
    }

    /**
     * Adds a directory that has execute permission to the result set.
     *
     * @param dir the directory path to record
     */
    void addExecutableDir(Path dir) {
        executableDirs.add(dir);
    }

    /**
     * Logs a summary of all executable files and directories found during the scan.
     */
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
