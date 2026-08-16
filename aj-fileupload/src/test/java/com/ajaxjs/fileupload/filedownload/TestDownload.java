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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link Download}, covering UTF-8 content disposition, path traversal
 * rejection, relative path resolution, symbolic link handling, and servlet-based
 * download response.
 */
class TestDownload {

    /**
     * Temporary directory used as the download root for tests.
     */
    @TempDir
    Path root;

    /**
     * Verifies that the download response includes a UTF-8 encoded content
     * disposition header with the correct file name and content length.
     *
     * @throws Exception if an I/O error occurs
     */
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

    /**
     * Verifies that requesting a directory or a relative path that escapes the
     * download root returns {@link HttpStatus#NOT_FOUND} or
     * {@link HttpStatus#BAD_REQUEST} respectively.
     *
     * @throws Exception if an I/O error occurs
     */
    @Test
    void rejectsDirectoryAndEscapingRelativePath() throws Exception {
        assertEquals(HttpStatus.NOT_FOUND, Download.download(root, "directory").getStatusCode());
        assertEquals(
                HttpStatus.BAD_REQUEST,
                Download.download(root, "../outside.txt", "outside.txt").getStatusCode()
        );
    }

    /**
     * Verifies that a file nested inside a subdirectory of the download root
     * can be downloaded successfully.
     *
     * @throws Exception if an I/O error occurs
     */
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

    /**
     * Verifies that a symbolic link pointing outside the download root is
     * rejected with {@link HttpStatus#BAD_REQUEST}.
     *
     * @throws Exception if an I/O error occurs
     */
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

    /**
     * Verifies that the servlet-based download correctly sets the status code,
     * content length, content disposition, and response body.
     *
     * @throws Exception if an I/O error occurs
     */
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

    /**
     * Verifies that the servlet-based download returns a 404 status when
     * the target is a directory.
     */
    @Test
    void servletDownloadReturnsNotFoundForDirectory() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        Download.downloadServlet(response, root.toFile());

        assertEquals(404, response.getStatus());
    }

    /**
     * Writes a file with the given name and content into the test root directory.
     *
     * @param filename the name of the file to create
     * @param content  the content to write
     * @return the path to the created file
     * @throws Exception if an I/O error occurs
     */
    private Path writeFile(String filename, String content) throws Exception {
        Path file = root.resolve(filename);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}