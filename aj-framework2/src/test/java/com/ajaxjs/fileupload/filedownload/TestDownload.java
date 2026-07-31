package com.ajaxjs.fileupload.filedownload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDownload {
    @TempDir
    Path root;

    @Test
    void buildsUtf8DispositionAndContentLength() throws Exception {
        Path file = writeFile("report.txt", "content");

        ResponseEntity<Resource> response = Download.download(file, "中文 报告.txt");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Files.size(file), response.getHeaders().getContentLength());
        String header = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertEquals("中文 报告.txt", ContentDisposition.parse(header).getFilename());
        assertTrue(header.contains("filename*="));
    }

    @Test
    void rejectsDirectoryAndEscapingRelativePath() throws Exception {
        assertEquals(HttpStatus.NOT_FOUND, Download.download(root, "directory").getStatusCode());
        assertEquals(
                HttpStatus.BAD_REQUEST,
                Download.download(root, "../outside.txt", "outside.txt").getStatusCode()
        );
    }

    @Test
    void safelyDownloadsFileRelativeToRoot() throws Exception {
        Path nested = Files.createDirectories(root.resolve("nested"));
        Path file = nested.resolve("report.txt");
        Files.write(file, "content".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<Resource> response =
                Download.download(root, "nested/report.txt", "report.txt");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Files.size(file), response.getHeaders().getContentLength());
    }

    @Test
    void rejectsSymbolicLinkEscapingDownloadRoot() throws Exception {
        Path outside = Files.createTempFile(root.getParent(), "outside-", ".txt");
        Path link = root.resolve("outside-link.txt");

        try {
            Files.createSymbolicLink(link, outside);
        } catch (java.io.IOException | UnsupportedOperationException | SecurityException e) {
            assumeTrue(false, "Symbolic links are not supported");
        }

        assertEquals(
                HttpStatus.BAD_REQUEST,
                Download.download(root, "outside-link.txt", "outside.txt").getStatusCode()
        );
    }

    @Test
    void servletDownloadSetsHeadersAndBody() throws Exception {
        Path file = writeFile("report.txt", "content");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Download.downloadServlet(response, file.toFile(), "中文报告.txt");

        assertEquals(200, response.getStatus());
        assertEquals(Files.size(file), response.getContentLengthLong());
        assertEquals(
                "中文报告.txt",
                ContentDisposition.parse(response.getHeader(HttpHeaders.CONTENT_DISPOSITION)).getFilename()
        );
        assertArrayEquals(Files.readAllBytes(file), response.getContentAsByteArray());
    }

    @Test
    void servletDownloadReturnsNotFoundForDirectory() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        Download.downloadServlet(response, root.toFile());

        assertEquals(404, response.getStatus());
    }

    private Path writeFile(String filename, String content) throws Exception {
        Path file = root.resolve(filename);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
