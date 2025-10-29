package com.ajaxjs.framework.fileupload.permission;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class PermissionCheck {
    /**
     * Check once avoids repeated work.
     */
    static boolean isAlreadyChecked;

    public static void check(String dir) {
        if (isAlreadyChecked)
            return;

        Path rootDir = Paths.get(dir); // 替换为你的存储目录

        if (!isPosixSupported()) {
            log.warn("The current system doesn't support POSIX permission, skip this check.");
            return;
        }

        if (!Files.exists(rootDir)) {
            log.warn("The current dir: {} doesn't exist.", dir);
            return;
        }

        if (!Files.isDirectory(rootDir)) {
            log.warn("The current dir: {} isn't a directory.", dir);
            return;
        }

        log.info("Going to check executable permission: " + rootDir);
        PermissionCheckResult result = new PermissionCheckResult();

        try {
            Files.walkFileTree(rootDir, new NoExecFileVisitor(result));
            result.printSummary();
        } catch (IOException e) {
            log.error("Error occurred when doing executable permission check on dir: " + dir, e);
            throw new UncheckedIOException("Error occurred when doing executable permission check on dir: " + dir, e);
        }

        isAlreadyChecked = true;
    }

    /**
     * 判断当前文件系统是否支持 POSIX 权限
     */
    public static boolean isPosixSupported() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }
}
