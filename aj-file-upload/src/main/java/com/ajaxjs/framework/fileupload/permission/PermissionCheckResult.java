package com.ajaxjs.framework.fileupload.permission;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class PermissionCheckResult {
    Set<Path> executableFiles = new HashSet<>();
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
                log.warn("📁 可执行目录: " + dir);

            for (Path file : executableFiles)
                log.warn("📄 可执行文件: " + file);
        }

        log.warn("共发现 {} 个可执行目录，{} 个可执行文件。", executableDirs.size(), executableFiles.size());
    }
}
