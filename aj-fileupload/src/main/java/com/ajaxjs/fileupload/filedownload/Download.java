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
 * File download utility for Spring MVC and Servlet environments.
 * <p>When a path contains client input, prefer the secure root entry point
 * {@link #download(Path, String, String)}.</p>
 */
@Slf4j
public class Download {
    /**
     * StreamingResponseBody is an interface added in Spring Framework 4.2 for handling asynchronous responses,
     * particularly suitable for streaming large files or large amounts of data.
     * It allows developers to write data directly to the HTTP response output stream without loading the entire
     * response content into memory, especially when handling large file downloads or exports, thereby avoiding
     * memory overflow and improving performance.
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
     * @param file     Trusted server-side file path
     * @param filename Download filename shown in the response; uses the actual filename if empty
     * @return 200 with file resource on success; 404 if the file is not readable; 400 on parameter or read error
     */
    public static ResponseEntity<Resource> download(Path file, String filename) {
        if (file == null)
            return ResponseEntity.badRequest().build();

        try {
            Path normalizedFile = file.toAbsolutePath().normalize();

            if (!isReadableRegularFile(normalizedFile)) {
                log.warn("File not found: {}", filename);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(normalizedFile.toUri());
            String contentType = probeContentType(normalizedFile);
            ContentDisposition disposition = contentDisposition(filename, normalizedFile);
            long contentLength = Files.size(normalizedFile);
            log.info("Downloading file: {}", disposition);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .contentLength(contentLength)
                    .body(resource);
        } catch (IOException | IllegalArgumentException e) {
            log.warn("File read error: {}", filename, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Resolves a client-supplied relative path under a trusted download root.
     *
     * @param root         Trusted server-side download root directory
     * @param relativePath Client-relative path under the root directory
     * @param downloadName Filename shown in the response; uses the actual filename if empty
     * @return Download response after security validation; 400 for path traversal, 404 for non-existent file
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
            log.warn("Download path resolution error: {}", relativePath, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Writes a trusted file to the Servlet response using the file's own name as the download filename.
     *
     * @param response     Servlet response
     * @param downloadFile Trusted server-side file
     * @throws IllegalArgumentException If {@code response} is {@code null}
     * @throws UncheckedIOException     If sending an error response or reading the file fails
     */
    public static void downloadServlet(HttpServletResponse response, File downloadFile) {
        downloadServlet(response, downloadFile, downloadFile == null ? null : downloadFile.getName());
    }

    /**
     * Writes a trusted file to the Servlet response.
     *
     * @param response     Servlet response
     * @param downloadFile Trusted server-side file
     * @param filename     Download filename shown in the response; uses the actual filename if empty
     * @throws IllegalArgumentException If the response or download filename is invalid
     * @throws UncheckedIOException     If setting the response or reading the file fails
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
            log.warn("Download file read error: {}", downloadFile.getName());
            throw new UncheckedIOException("File read error", e);
        }
    }

    /**
     * Checks whether the given path is a regular file and is readable.
     *
     * @param file the path to check
     * @return {@code true} if the path is a regular file and is readable
     */
    static boolean isReadableRegularFile(Path file) {
        return Files.isRegularFile(file) && Files.isReadable(file);
    }

    /**
     * Probes the MIME content type of the given file.
     *
     * @param file the file to probe
     * @return the detected content type, or {@link MediaType#APPLICATION_OCTET_STREAM_VALUE} if unknown
     * @throws IOException if an I/O error occurs during probing
     */
    static String probeContentType(Path file) throws IOException {
        String contentType = Files.probeContentType(file);

        return contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType;
    }

    /**
     * Builds a {@link ContentDisposition} header for the download response.
     *
     * @param filename the desired download filename
     * @param file     the fallback file used when {@code filename} is empty
     * @return the attachment content disposition
     */
    static ContentDisposition contentDisposition(String filename, Path file) {
        String safeFilename = sanitizeDownloadName(filename, file);

        return ContentDisposition.attachment()
                .filename(safeFilename, StandardCharsets.UTF_8)
                .build();
    }

    /**
     * Sanitizes the download filename by removing path separators, control characters,
     * and invalid names such as {@code .} and {@code ..}.
     *
     * @param filename the desired download filename
     * @param file     the fallback file used when {@code filename} is empty
     * @return a sanitized, safe filename
     * @throws IllegalArgumentException if the filename contains control characters or is invalid
     */
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
