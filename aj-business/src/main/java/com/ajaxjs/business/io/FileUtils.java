package com.ajaxjs.business.io;


import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {
    /*
     * 不论在windows还是linux下,仅凭字符串比较判断两个文件路径是否相等是不靠谱的。 因为有link，Disk
     * map等技术的存在，两个不同的路径有可能指向同一个文件。 NIO提供了Files.isSameFile方法可以准确判断两个路径是否指向同一个文件/文件夹.
     * 以下示例是利用Files.isSameFile方法判断一个文件/文件夹是否为在另一个文件夹下的方法。
     */

    /**
     * 判断 sub 是否与 parent 相等或在其之下<br>
     * parent 必须存在，且必须是 directory,否则抛出{@link IllegalArgumentException}
     *
     * @param parent
     * @param sub
     * @return
     * @throws IOException
     */
    public static boolean sameOrSub(Path parent, Path sub) throws IOException {
        if (null == parent)
            throw new NullPointerException("parent is null");

        if (!Files.exists(parent) || !Files.isDirectory(parent))
            throw new IllegalArgumentException(String.format("the parent not exist or not directory %s", parent));

        while (null != sub) {
            if (Files.exists(sub) && Files.isSameFile(parent, sub))
                return true;

            sub = sub.getParent();
        }

        return false;
    }

    /**
     * 判断sub是否在parent之下的文件或子文件夹<br>
     * parent必须存在，且必须是directory,否则抛出{@link IllegalArgumentException}
     *
     * @param parent
     * @param sub
     * @return
     * @throws IOException
     * @see {@link #sameOrSub(Path, Path)}
     */
    public static boolean isSub(Path parent, Path sub) throws IOException {
        return null != sub && sameOrSub(parent, sub.getParent());
    }

    /*
     * java.nio.file.Files.isWritable方法用于测试一个文件是否可写。
     * 但是对于文件夹，这个办法并不能用来测试文件夹是否可以创建子文件夹或文件。 比如对于匿名(只读)访问一个网络共享文件夹,isWritable返回是true
     * 所以如果想判断一个文件夹是不是真的可写，这个办法是不靠谱的。 怎么办呢？看来只有去尝试创建文件和文件夹才能真判断文件夹是否可写了，
     * 于是想到了用于创建临时文件夹和临时文件的两个方法Files.createTempDirectory,Files.createTempFile，
     * 用这两个方法尝试创建临时文件夹和临时文件，如果成功并且能删除就说明该文件夹可以可写。
     */

    /**
     * 判断一个文件夹是否可创建文件/文件夹及可删除
     *
     * @param dir
     * @return
     */
    public static boolean isWritableDirectory(Path dir) {
        if (null == dir)
            throw new IllegalArgumentException("the argument 'dir' must not be null");

        if (!Files.isDirectory(dir))
            throw new IllegalArgumentException("the argument 'dir' must be a exist directory");

        try {
            Path tmpDir = Files.createTempDirectory(dir, null);
            Files.delete(tmpDir);
        } catch (IOException e) {
            return false;
        }

        try {
            Path tmpFile = Files.createTempFile(dir, null, null);
            Files.delete(tmpFile);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /*
     * 使用于NIO实现文件夹的复制/移动,删除 从java 1.7开始，java提供了java.noi.file.Files类用于更方便的实现文件/文件夹操作。
     * 在Files中提供了丰富的静态方法用于文件操作,Files也提供了文件移动和复制操作(Files.move,Files.copy)，
     * 但是对于不为的空文件夹,不能调用Files.move,Files.copy实现文件夹下所有文件的复制和移动。
     * 根据Files.move,Files.copy的说明，如果要移动/复制包含子目录的文件夹，
     * 需要用Files.walkFileTree方法配合Files.move,Files.copy来实现。
     *
     * public void testCopy() throws IOException{ Path start =
     * Paths.get("d:\\tmp\\storeroot\\origin"); Path target =
     * Paths.get("E:\\tmp\\storeroot"); NioFileUtil.copyDir(start, target); }
     */

    /**
     * 复制文件夹
     *
     * @param source
     * @param target
     * @param options
     * @throws IOException
     * @see {@link #operateDir(boolean, Path, Path, CopyOption...)}
     */
    public static void copyDir(Path source, Path target, CopyOption... options) throws IOException {
        operateDir(false, source, target, options);
    }

    /**
     * 移动文件夹
     *
     * @param source
     * @param target
     * @param options
     * @throws IOException
     * @see {@link #operateDir(boolean, Path, Path, CopyOption...)}
     */
    public static void moveDir(Path source, Path target, CopyOption... options) throws IOException {
        operateDir(true, source, target, options);
    }

    /**
     * 复制/移动文件夹
     *
     * @param move    操作标记，为true时移动文件夹,否则为复制
     * @param source  要复制/移动的源文件夹
     * @param target  源文件夹要复制/移动到的目标文件夹
     * @param options 文件复制选项
     * @throws IOException
     * @see Files#move(Path, Path, CopyOption...)
     * @see Files#copy(Path, Path, CopyOption...)
     * @see Files#walkFileTree(Path, java.nio.file.FileVisitor)
     */
    public static void operateDir(boolean move, Path source, Path target, CopyOption... options) throws IOException {
        if (null == source || !Files.isDirectory(source))
            throw new IllegalArgumentException("source must be directory");
        Path dest = target.resolve(source.getFileName());
        // 如果相同则返回
        if (Files.exists(dest) && Files.isSameFile(source, dest))
            return;
        // 目标文件夹不能是源文件夹的子文件夹
        // isSub方法实现参见另一篇博客 http://blog.csdn.net/10km/article/details/54425614
        if (isSub(source, dest))
            throw new IllegalArgumentException("dest must not  be sub directory of source");
        boolean clear = true;
        for (CopyOption option : options)
            if (StandardCopyOption.REPLACE_EXISTING == option) {
                clear = false;
                break;
            }
        // 如果指定了REPLACE_EXISTING选项则不清除目标文件夹
        if (clear)
            deleteIfExists(dest);
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // 在目标文件夹中创建dir对应的子文件夹
                Path subDir = 0 == dir.compareTo(source) ? dest
                        : dest.resolve(dir.subpath(source.getNameCount(), dir.getNameCount()));
                Files.createDirectories(subDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (move)
                    Files.move(file, dest.resolve(file.subpath(source.getNameCount(), file.getNameCount())), options);
                else
                    Files.copy(file, dest.resolve(file.subpath(source.getNameCount(), file.getNameCount())), options);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // 移动操作时删除源文件夹
                if (move)
                    Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    /**
     * 强制删除文件/文件夹(含不为空的文件夹)<br>
     *
     * @param dir
     * @throws IOException
     * @see Files#deleteIfExists(Path)
     * @see Files#walkFileTree(Path, java.nio.file.FileVisitor)
     */
    public static void deleteIfExists(Path dir) throws IOException {
        try {
            Files.deleteIfExists(dir);
        } catch (DirectoryNotEmptyException e) {
            Files.walkFileTree(dir, new SimpleFileVisitor() {
//                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

//                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return super.postVisitDirectory(dir, exc);
                }
            });
        }
    }
}
