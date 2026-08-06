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
 * 遍历目录树并收集具有任一执行权限位的路径。
 */
public class NoExecFileVisitor extends SimpleFileVisitor<Path> {
    private final PermissionCheckResult result;

    /**
     * 创建文件访问器。
     *
     * @param result 扫描结果收集器
     */
    public NoExecFileVisitor(PermissionCheckResult result) {
        this.result = result;
    }

    /**
     * 检查普通文件的 POSIX 执行权限。
     *
     * @param file  当前文件
     * @param attrs 文件属性
     * @return 始终返回 {@link FileVisitResult#CONTINUE}
     * @throws IOException 读取权限失败时抛出
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (hasExecutePermission(file))
            result.addExecutableFile(file);

        return FileVisitResult.CONTINUE;
    }

    /**
     * 检查目录的 POSIX 执行权限。
     *
     * @param dir   当前目录
     * @param attrs 目录属性
     * @return 始终返回 {@link FileVisitResult#CONTINUE}
     * @throws IOException 读取权限失败时抛出
     */
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
