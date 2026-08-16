package com.ajaxjs.fileupload.filedownload;

import com.ajaxjs.util.io.DataWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Spring MVC 和 Servlet 环境下的文件下载工具。
 * <p>路径包含客户端输入时，应优先使用
 * {@link #download(Path, String, String)} 的安全根目录入口。</p>
 */
@Slf4j
public class Download {
    /**
     * StreamingResponseBody是Spring框架从4.2版本增加的一个个用于处理异步响应的接口,特别适用于需要流式传输大文件或大量数据的场景。
     * 它允许开发者直接将数据写入HTTP响应的输出流,而无需将整个响应内容加载到内存中,
     * 尤其是在处理大文件下载或导出时,从而避免了内存溢出,并提高了程序性能
     * <a href="https://mp.weixin.qq.com/s/Q88V8wYRaEduRSZHE0XKFQ">...</a>
     * <a href="https://mp.weixin.qq.com/s/jvPQH7Wzue1eRl2R51ZXIQ">...</a>
     * <a href="https://github.com/Linyuzai/concept/wiki/Concept-Download-2">...</a>
     * <a href="https://mp.weixin.qq.com/s/ZF6V_mhdK3ZaUnQSRoMpTQ">...</a>
     *
     * @return ResponseEntity
     */
    ResponseEntity<?> down() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("orders_" + System.currentTimeMillis() + ".xlsx").build());

        StreamingResponseBody body = outputStream -> {
        };
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    /**
     * Downloads a trusted server-side path.
     * Use {@link #download(Path, String, String)} when any path component comes from a client.
     *
     * @param file     服务端可信文件路径
     * @param filename 响应中展示的下载文件名；为空时使用实际文件名
     * @return 成功时为 200 和文件资源；文件不可读时为 404；参数或读取异常时为 400
     */
    public static ResponseEntity<Resource> download(Path file, String filename) {
        if (file == null)
            return ResponseEntity.badRequest().build();

        try {
            Path normalizedFile = file.toAbsolutePath().normalize();

            if (!isReadableRegularFile(normalizedFile)) {
                log.warn("文件不存在：{}", filename);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(normalizedFile.toUri());
            String contentType = probeContentType(normalizedFile);
            ContentDisposition disposition = contentDisposition(filename, normalizedFile);
            long contentLength = Files.size(normalizedFile);
            log.info("下载文件：{}", disposition);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .contentLength(contentLength)
                    .body(resource);
        } catch (IOException | IllegalArgumentException e) {
            log.warn("文件读取异常：{}", filename, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Resolves a client-supplied relative path under a trusted download root.
     *
     * @param root         服务端可信下载根目录
     * @param relativePath 根目录下的客户端相对路径
     * @param downloadName 响应中展示的文件名；为空时使用实际文件名
     * @return 安全校验后的下载响应；越界路径为 400，不存在的文件为 404
     */
    public static ResponseEntity<Resource> download(Path root, String relativePath, String downloadName) {
        if (root == null || relativePath == null || relativePath.trim().isEmpty())
            return ResponseEntity.badRequest().build();

        try {
            Path relative = Paths.get(relativePath);

            if (relative.isAbsolute())
                return ResponseEntity.badRequest().build();

            Path normalizedRoot = root.toAbsolutePath().normalize();
            Path candidate = normalizedRoot.resolve(relative).normalize();

            if (!candidate.startsWith(normalizedRoot))
                return ResponseEntity.badRequest().build();

            if (!Files.isDirectory(normalizedRoot) || !Files.exists(candidate, LinkOption.NOFOLLOW_LINKS))
                return ResponseEntity.notFound().build();

            Path realRoot = normalizedRoot.toRealPath();
            Path realFile = candidate.toRealPath();

            if (!realFile.startsWith(realRoot))
                return ResponseEntity.badRequest().build();

            return download(realFile, downloadName);
        } catch (IOException | IllegalArgumentException e) {
            log.warn("下载路径解析异常：{}", relativePath, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 将可信文件写入 Servlet 响应，并使用文件自身名称作为下载名。
     *
     * @param response     Servlet 响应
     * @param downloadFile 服务端可信文件
     * @throws IllegalArgumentException {@code response} 为 {@code null} 时抛出
     * @throws UncheckedIOException     设置错误响应或读取文件失败时抛出
     */
    public static void downloadServlet(HttpServletResponse response, File downloadFile) {
        downloadServlet(response, downloadFile, downloadFile == null ? null : downloadFile.getName());
    }

    /**
     * 将可信文件写入 Servlet 响应。
     *
     * @param response     Servlet 响应
     * @param downloadFile 服务端可信文件
     * @param filename     响应中展示的下载文件名；为空时使用实际文件名
     * @throws IllegalArgumentException 响应或下载名不合法时抛出
     * @throws UncheckedIOException     设置响应或读取文件失败时抛出
     */
    public static void downloadServlet(HttpServletResponse response, File downloadFile, String filename) {
        if (response == null)
            throw new IllegalArgumentException("The HTTP response is required.");

        if (downloadFile == null || !isReadableRegularFile(downloadFile.toPath())) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to send the file-not-found response.", e);
            }
        }

        Path file = downloadFile.toPath().toAbsolutePath().normalize();

        try (InputStream in = Files.newInputStream(file)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(probeContentType(file));
            response.setContentLengthLong(Files.size(file));
            response.setHeader(
                    HttpHeaders.CONTENT_DISPOSITION,
                    contentDisposition(filename, file).toString()
            );

            OutputStream out = response.getOutputStream();
            new DataWriter(out).write(in);
            out.flush();
        } catch (IOException e) {
            log.warn("下载的文件读取异常：{}", downloadFile.getName());
            throw new UncheckedIOException("文件读取异常", e);
        }
    }

    static boolean isReadableRegularFile(Path file) {
        return Files.isRegularFile(file) && Files.isReadable(file);
    }

    static String probeContentType(Path file) throws IOException {
        String contentType = Files.probeContentType(file);

        return contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType;
    }

    static ContentDisposition contentDisposition(String filename, Path file) {
        String safeFilename = sanitizeDownloadName(filename, file);

        return ContentDisposition.attachment()
                .filename(safeFilename, StandardCharsets.UTF_8)
                .build();
    }

    static String sanitizeDownloadName(String filename, Path file) {
        String name = filename;

        if (name == null || name.trim().isEmpty())
            name = file.getFileName().toString();

        for (int i = 0; i < name.length(); i++)
            if (Character.isISOControl(name.charAt(i)))
                throw new IllegalArgumentException("The download filename contains control characters.");

        name = name.trim().replace('\\', '/');
        int lastSeparator = name.lastIndexOf('/');

        if (lastSeparator >= 0)
            name = name.substring(lastSeparator + 1);

        if (name.isEmpty() || ".".equals(name) || "..".equals(name))
            throw new IllegalArgumentException("The download filename is invalid.");

        return name;
    }
}
