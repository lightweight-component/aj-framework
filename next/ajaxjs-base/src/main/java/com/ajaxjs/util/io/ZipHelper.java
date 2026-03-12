package com.ajaxjs.util.io;

import com.ajaxjs.util.CommonConstant;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.*;

/**
 * ZIP Compression and Decompression Utility Class
 * <p>
 * This class provides comprehensive functionality for working with ZIP files, including
 * extracting ZIP contents, creating ZIP archives from files and directories,
 * handling Chinese filenames, and utility methods for ZIP file operations.
 * It supports both STORED (uncompressed) and DEFLATED (compressed) modes.
 */
@Slf4j
public class ZipHelper {
    /**
     * 解压文件
     *
     * @param save    解压文件的路径，必须为目录
     * @param zipFile 输入的解压文件路径，例如 C:/temp/foo.zip 或 c:\\temp\\bar.zip
     */
    public static void unzip(String save, String zipFile) {
        long start = System.currentTimeMillis();
        new FileHelper(save).createDirectory();

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Paths.get(zipFile)))) {
            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {
                File newFile = new File(save + File.separator + ze.getName());

                if (ze.isDirectory()) // 大部分网络上的源码，这里没有判断子目录
                    newFile.mkdirs();
                else {
//					new File(newFile.getParent()).mkdirs();
                    initFolder(newFile);
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        new DataWriter(fos).write(zis);
                    }
                }

//				ze = zis.getNextEntry();
            }

            zis.closeEntry();
        } catch (IOException e) {
            log.warn("unzip", e);
            throw new UncheckedIOException(e);
        }

        log.info("解压缩完成，耗时：{}ms，保存在{}", System.currentTimeMillis() - start, save);
    }

    /**
     * 解压文件
     *
     * @param save        解压文件的路径，必须为目录
     * @param zipFilePath 输入的解压文件路径，例如 C:/temp/foo.zip 或 c:\\temp\\bar.zip
     */
    public static void unzipWithChineseFilename(String save, String zipFilePath) {
        long start = System.currentTimeMillis();
        new FileHelper(save).createDirectory();

        try (ZipFile zipFile = new ZipFile(zipFilePath, Charset.forName("GBK"))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File newFile = new File(save, entry.getName());

                if (entry.isDirectory())
                    newFile.mkdirs();
                else {
                    initFolder(newFile);
                    try (InputStream is = zipFile.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(newFile)) {
                        new DataWriter(fos).write(is);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("unzip", e);
            throw new UncheckedIOException(e);
        }

        log.info("解压缩完成，耗时：{}ms，保存在{}", System.currentTimeMillis() - start, save);
    }

    /**
     * Compress an array of files into a ZIP archive
     * <p>
     * Takes an array of File objects and compresses them into a single ZIP file.
     * All files are added to the root of the ZIP archive without preserving directory structure.
     *
     * @param fileContent Array of files to be compressed into the ZIP archive
     * @param saveZip     Path where the resulting ZIP file will be saved
     * @param useStore    Compression mode: true for STORED (no compression), false for DEFLATED (standard compression)
     */
    public static void zipFile(File[] fileContent, String saveZip, boolean useStore) {
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(saveZip)));
             ZipOutputStream zipOut = new ZipOutputStream(bos)) {

            for (File fc : fileContent)
                addFileToZip(fc, fc.getName(), zipOut, useStore);
        } catch (IOException e) {
            log.warn("一维文件数组压缩为 ZIP", e);
        }
    }

    /**
     * 递归压缩目录为ZIP
     *
     * @param sourceDir 目录路径
     * @param saveZip   目标 zip 文件路径
     * @param useStore  true: 仅存储(STORED)，false: 标准压缩(DEFLATED)
     */
    public static void zipDirectory(String sourceDir, String saveZip, boolean useStore) {
        File dir = new File(sourceDir);

        if (!dir.exists() || !dir.isDirectory())
            throw new IllegalArgumentException("Source directory does not exist or is not a directory: " + sourceDir);

        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(saveZip)));
             ZipOutputStream zipOut = new ZipOutputStream(bos)) {
            String basePath = dir.getCanonicalPath();
            zipDirectoryRecursive(dir, basePath, zipOut, useStore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 目录压缩，用于递归
     */
    private static void zipDirectoryRecursive(File file, String basePath, ZipOutputStream zipOut, boolean useStore) throws IOException {
        String relativePath = basePath.equals(file.getCanonicalPath())
                ? CommonConstant.EMPTY_STRING
                : file.getCanonicalPath().substring(basePath.length() + 1).replace(File.separatorChar, '/');

        if (file.isDirectory()) {
            File[] files = file.listFiles();

            if (files != null && files.length == 0 && !relativePath.isEmpty()) {
                ZipEntry entry = new ZipEntry(relativePath + "/"); // 空目录也要加入Zip
                zipOut.putNextEntry(entry);
                zipOut.closeEntry();
            } else if (files != null) {
                for (File child : files)
                    zipDirectoryRecursive(child, basePath, zipOut, useStore);
            }
        } else
            addFileToZip(file, relativePath, zipOut, useStore);
    }

    /**
     * 单文件添加到 zip
     */
    private static void addFileToZip(File file, String zipEntryName, ZipOutputStream zipOut, boolean useStore) throws IOException {
        try (BufferedInputStream bin = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
            ZipEntry entry = new ZipEntry(zipEntryName);

            if (useStore) {
                entry.setMethod(ZipEntry.STORED);
                entry.setSize(file.length());
                entry.setCrc(getFileCRCCode(file));
            } else
                entry.setMethod(ZipEntry.DEFLATED);// // DEFLATED 模式不需要设置 size 和 crc，ZipOutputStream 会自动处理

            zipOut.putNextEntry(entry);

            byte[] buffer = new byte[8192];
            int len;
            while ((len = bin.read(buffer)) != -1)
                zipOut.write(buffer, 0, len);

            zipOut.closeEntry();
        }
    }

    /**
     * 获取 CRC32
     * CheckedInputStream 一种输入流，它还维护正在读取的数据的校验和。然后可以使用校验和来验证输入数据的完整性。
     *
     * @param file 必须是文件，不是目录
     */
    private static long getFileCRCCode(File file) {
        CRC32 crc32 = new CRC32();

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(file.toPath()));
             CheckedInputStream checkedinputstream = new CheckedInputStream(bufferedInputStream, crc32)) {
            while (checkedinputstream.read() != -1) {
                // 只需遍历即可统计
            }
        } catch (IOException e) {
            log.warn("getFileCRCCode", e);
        }

        return crc32.getValue();
    }

    /**
     * 检测文件所在的目录是否存在，如果没有则建立。可以跨多个未建的目录
     *
     * @param file 必须是文件，不是目录
     */
    public static void initFolder(File file) {
        if (file.isDirectory())
            throw new IllegalArgumentException("参数必须是文件，不是目录");

        new FileHelper(file).createDirectory();
    }

    /**
     * 检测文件所在的目录是否存在，如果没有则建立。可以跨多个未建的目录
     *
     * @param file 必须是文件，不是目录
     */
    public static void initFolder(String file) {
        initFolder(new File(file));
    }

    /**
     * 检查给定的字节数组是否为 Zip 文件的魔数。
     * 魔数是一段特定的字节序列，用于标识文件的类型。
     * 此方法专门检查 Zip 文件的魔数，该魔数由 PK\03\04 组成。
     *
     * @param magicNumber 字节数组，代表待检查的文件的前四个字节。
     * @return 如果字节数组的前四个字节与 Zip 文件的魔数匹配，则返回 true；否则返回 false。
     */
    private static boolean isZipFile(byte[] magicNumber) {
        // 比较字节数组的前四个字
        return magicNumber[0] == 0x50 && magicNumber[1] == 0x4b && magicNumber[2] == 0x03 && magicNumber[3] == 0x04;
    }

//    private static final String ZIP_MAGIC_NUMBER = "504B0304";

    /**
     * 判断给定的文件路径是否为 ZIP 文件。
     *
     * @param filePath 文件路径
     * @return 如果是 ZIP 文件则返回 true，否则返回 false
     */
    public static boolean isZipFile(String filePath) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            byte[] magicNumber = new byte[4];

            if (inputStream.read(magicNumber, 0, 4) == 4)  // 读取到了 4 个字节
                return magicNumber[0] == 0x50 && magicNumber[1] == 0x4b && magicNumber[2] == 0x03 && magicNumber[3] == 0x04;
            // ZIP_MAGIC_NUMBER.equalsIgnoreCase(StreamHelper.bytesToHexStr(magicNumber));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    /**
     * 压缩单个文件为ZIP
     *
     * @param sourceFile 源文件路径
     * @param saveZip    目标ZIP文件路径
     * @param useStore   true: 仅存储(STORED)，false: 标准压缩(DEFLATED)
     */
    public static void zipSingleFile(String sourceFile, String saveZip, boolean useStore) {
        File file = new File(sourceFile);

        if (!file.exists() || file.isDirectory())
            throw new IllegalArgumentException("Source file does not exist or is a directory: " + sourceFile);

        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(saveZip)));
             ZipOutputStream zipOut = new ZipOutputStream(bos)) {
            addFileToZip(file, file.getName(), zipOut, useStore);
        } catch (IOException e) {
            log.warn("单文件压缩为 ZIP 失败: " + sourceFile, e);
            throw new UncheckedIOException(e);
        }
    }

}
