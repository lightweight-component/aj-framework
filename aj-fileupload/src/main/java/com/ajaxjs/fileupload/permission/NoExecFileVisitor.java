package com.ajaxjs.fileupload.permission;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * Traverses the directory tree and collects paths that have any execute permission bit set.
 */
public class NoExecFileVisitor extends SimpleFileVisitor<Path> {
    /**
     * Collector for scan results.
     */
    private final PermissionCheckResult result;

    /**
     * Creates a file visitor.
     *
     * @param result scan result collector
     */
    public NoExecFileVisitor(PermissionCheckResult result) {
        this.result = result;
    }

    /**
     * Checks POSIX execute permissions for regular files.
     *
     * @param file  current file
     * @param attrs file attributes
     * @return always returns {@link FileVisitResult#CONTINUE}
     * @throws IOException thrown when reading permissions fails
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (hasExecutePermission(file))
            result.addExecutableFile(file);

        return FileVisitResult.CONTINUE;
    }

    /**
     * Checks POSIX execute permissions for directories.
     *
     * @param dir   current directory
     * @param attrs directory attributes
     * @return always returns {@link FileVisitResult#CONTINUE}
     * @throws IOException thrown when reading permissions fails
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (hasExecutePermission(dir))
            result.addExecutableDir(dir);

        return FileVisitResult.CONTINUE;
    }

    /**
     * Checks whether the path has execute permission (true if any of owner/group/others has execute permission).
     */
    static boolean hasExecutePermission(Path path) throws IOException {
        try {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);

            return perms.contains(PosixFilePermission.OWNER_EXECUTE) ||
                    perms.contains(PosixFilePermission.GROUP_EXECUTE) ||
                    perms.contains(PosixFilePermission.OTHERS_EXECUTE);
        } catch (UnsupportedOperationException e) {
            System.err.println("不支持 POSIX 权限: " + path); // Theoretically should not happen because we already checked POSIX support
            return false;
        }
    }
}