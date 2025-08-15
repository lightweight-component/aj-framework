package com.ajaxjs.framework.mvc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;

/**
 * @author <a href="https://mp.weixin.qq.com/s/AwIrEuFqEJsn0eceBxXCQQ">...</a>
 */
@Slf4j
public class Download {
    /**
     * 文件下载
     * 将文件内容作为流（Stream）传输，而不是一次性将整个文件加载到内存中
     *
     * @param filePath 文件路径
     * @return 响应体
     */
    public static ResponseEntity<InputStreamResource> downloadFileByInputStreamResource(String filePath) {
        return downloadFile(filePath, file -> {
            try {
                return new InputStreamResource(Files.newInputStream(file.toPath()));
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                throw new UncheckedIOException(e);
            }
        });
    }

    /**
     * 文件下载
     * Spring 提供的文件系统资源包装类，优化了文件的读取和传输过程
     *
     * @param filePath 文件路径
     * @return 响应体
     */
    public static ResponseEntity<FileSystemResource> downloadFileByFileSystemResource(String filePath) {
        return downloadFile(filePath, FileSystemResource::new);
    }

    public static <T> ResponseEntity<T> downloadFile(String filePath, Function<File, T> cb) {
        File file = new File(filePath);

        if (!file.exists())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        HttpHeaders headers = getHeader(file.getName(), file.length(), getFileContentType(file));
        return ResponseEntity.ok().headers(headers).body(cb.apply(file));
    }

    /**
     * 文件下载
     * 从 URL 地址下载文件
     * <a href="https://mp.weixin.qq.com/s/-htWPBJHlkhRIO60PCoh9g">...</a>
     *
     * @param url 文件 URL 地址
     * @return 响应体
     */
    public static ResponseEntity<Resource> downloadFileFromURL(String url) {
        // 1、Spring提供的资源包装类,远程 URL地址的资源下载
        Resource resource;

        try {
            resource = new UrlResource(url);
        } catch (MalformedURLException e) {
            log.warn(e.getMessage(), e);
            throw new UncheckedIOException(e);
        }

        if (!resource.exists())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        // 3、设置响应头
        HttpHeaders headers = getHeader(resource.getFilename(), 0, null);

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    /**
     * 分块下载（Range Requests）：
     * 支持 HTTP 的 Range 头，通过将文件分成多个部分并行下载，可以显著提高下载速度。
     * <a href="https://mp.weixin.qq.com/s/EKlNd0uVkMU6RgJfCfd2zQ">...</a>
     *
     * @param headers
     * @param filePath 文件路径
     * @return 响应体
     */
    public static ResponseEntity<FileSystemResource> downloadRangeFile(@RequestHeader HttpHeaders headers, String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        long fileLength = file.length();
        String rangeHeader = headers.getFirst(HttpHeaders.RANGE);

        //1、第一次请求，获取文件大小
        if (!StringUtils.hasText(rangeHeader))
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength))
                    .body(null);

        // 2、第一次之后的请求，分块下载文件
        // 2.1、获取文件的开始位置和结束位置
        List<HttpRange> ranges = headers.getRange();
        HttpRange range = ranges.get(0);
        long start = range.getRangeStart(fileLength);
        long end = range.getRangeEnd(fileLength);

        // 2.2、获取本次文件下载大小
        long remaining = end - start + 1;
        FileSystemResource resource = new FileSystemResource(file) {
            @Override
            public long contentLength() {
                return remaining;
            }
        };

        HttpHeaders httpHeaders = getHeader(file.getName(), remaining, null);
        // 分块下载的三个关键头部设置
        httpHeaders.add("Accept-Ranges", "bytes");
        httpHeaders.add(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + file.length());

        return ResponseEntity.status(206).headers(httpHeaders).body(resource);
    }

    /**
     * 4. 断点续传下载
     * 断点续传技术可以在下载过程中断开后，从中断的地方继续下载，避免重复下载已下载的部分数据。
     */
    public static ResponseEntity<Resource> downloadFileResumable(@RequestHeader HttpHeaders headers, String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        // 获取文件大小
        long fileLength = file.length();
        String rangeHeader = headers.getFirst(HttpHeaders.RANGE);
        String filename = file.getName();

        //2、第一次请求，获取文件大小
        if (!StringUtils.hasText(rangeHeader))
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength))
//                    .header("filename", filename)
                    .body(null);

        // 2、第二次请求，分块下载
        List<HttpRange> ranges = headers.getRange();
        HttpRange range = ranges.get(0);
        // 获取分块信息，开始字节数和结束字节数
        long start = range.getRangeStart(fileLength);
        long end = range.getRangeEnd(fileLength);

        long remaining = end - start + 1;
        // 3、读取部分文件流
        FileSystemResource resource = new FileSystemResource(file) {
            @Override
            public long contentLength() {
                return remaining;
            }
        };

        // 4、设置头部属性
        HttpHeaders httpHeaders = getHeader(filename, remaining, null);
        // 分块下载的三个关键头部设置
        httpHeaders.add("Accept-Ranges", "bytes");
        httpHeaders.add(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + file.length());

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(httpHeaders).body(resource);
    }

    /**
     * 构建 HTTP 响应头，用于文件下载
     *
     * @param filename    文件名，用于在下载时显示给用户
     * @param fileLength  文件大小，用于显示下载进度
     * @param contentType 文件的 MIME 类型，指定文件的内容格式
     * @return 包含文件下载相关信息的 HTTP 响应头
     */
    public static HttpHeaders getHeader(String filename, long fileLength, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        String encodedFileName = UriUtils.encode(filename, "UTF-8");

        // 指定文件名和附件方式，告知浏览器这是一个下载文件而非直接显示的内容。
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + encodedFileName);

        if (fileLength != 0)
            // 指定文件大小，有助于浏览器在下载时显示下载进度。
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength));

        headers.add(HttpHeaders.CONTENT_TYPE, StringUtils.hasText(contentType) ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return headers;
    }

    /**
     * 根据文件扩展名确定 MediaType
     */
    public static String getFileContentType(File file) {
        String contentType;

        try {
            contentType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            throw new UncheckedIOException(e);
        }

        if (StringUtils.hasText(contentType))
            // 设置 ContentType为application/octet-stream：用于通用的二进制数据下载，适用于大多数文件下载场景。
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return contentType;
    }
}
