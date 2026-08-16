package com.ajaxjs.fileupload.permission;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * POSIX 上传目录执行权限扫描器。
 * <p>不支持 POSIX 属性视图的平台会跳过检查。当前实现仅执行一次全局扫描，
 * 其限制记录在模块 {@code to-fix.md} 中。</p>
 */
@Slf4j
public class PermissionCheck {
    /**
     * Check once avoids repeated work.
     */
    static boolean isAlreadyChecked;

    /**
     * 扫描目录树并记录具有执行权限的目录和文件。
     *
     * @param dir 待扫描目录路径
     * @throws UncheckedIOException 遍历目录失败时抛出
     */
    public static void check(String dir) {
        if (isAlreadyChecked)
            return;

        Path rootDir = Paths.get(dir); // 替换为你的存储目录

        if (!FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
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

        log.info("Going to check executable permission: {}", rootDir);
        PermissionCheckResult result = new PermissionCheckResult();

        try {
            Files.walkFileTree(rootDir, new NoExecFileVisitor(result));
            result.printSummary();
        } catch (IOException e) {
            log.error("Error occurred when doing executable permission check on dir: {}", dir, e);
            throw new UncheckedIOException("Error occurred when doing executable permission check on dir: " + dir, e);
        }

        isAlreadyChecked = true;
    }
}
