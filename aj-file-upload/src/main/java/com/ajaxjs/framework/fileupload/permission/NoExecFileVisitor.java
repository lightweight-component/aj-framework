package com.ajaxjs.framework.fileupload.permission;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class NoExecFileVisitor extends SimpleFileVisitor<Path> {
    private final PermissionCheckResult result;

    public NoExecFileVisitor(PermissionCheckResult result) {
        this.result = result;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (hasExecutePermission(file))
            result.addExecutableFile(file);

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (hasExecutePermission(dir))
            result.addExecutableDir(dir);

        return FileVisitResult.CONTINUE;
    }

    /**
     * 检查路径是否有执行权限（owner/group/others 任一有执行权限即为 true）
     */
    private static boolean hasExecutePermission(Path path) throws IOException {
        try {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);

            return perms.contains(PosixFilePermission.OWNER_EXECUTE) ||
                    perms.contains(PosixFilePermission.GROUP_EXECUTE) ||
                    perms.contains(PosixFilePermission.OTHERS_EXECUTE);
        } catch (UnsupportedOperationException e) {
            System.err.println("不支持 POSIX 权限: " + path); // 理论上不会发生，因为我们已检查 POSIX 支持
            return false;
        }
    }

}
