package com.ajaxjs.fileupload.permission;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * POSIX upload directory executes permission scanner.
 * <p>Platforms that do not support POSIX attribute views will skip the check. The current implementation
 * performs only a single global scan, and its limitations are documented in the {@code to-fix.md} module.</p>
 */
@Slf4j
public class PermissionCheck {
    /**
     * Check once avoids repeated work.
     */
    static boolean isAlreadyChecked;

    /**
     * Scan the directory tree and record directories and files with execute permission.
     *
     * @param dir path of the directory to scan
     * @throws UncheckedIOException if directory traversal fails
     */
    public static void check(String dir) {
        if (isAlreadyChecked)
            return;

        Path rootDir = Paths.get(dir); // Replace with your storage directory

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

    /**
     * Determine whether the default file system supports POSIX permission attributes.
     *
     * @return {@code true} if supported
     */
    public static boolean isPosixSupported() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }
}
