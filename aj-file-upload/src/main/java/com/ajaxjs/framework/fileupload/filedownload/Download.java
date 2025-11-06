package com.ajaxjs.framework.fileupload.filedownload;

import com.ajaxjs.util.io.DataWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public static ResponseEntity<Resource> download(Path file, String filename) {
        try {
            Resource resource = new UrlResource(file.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("文件不存在：{}", filename);
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(file);

            if (contentType == null)
                contentType = "application/octet-stream"; // fallback

            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, contentType).body(resource);
        } catch (IOException e) {
            log.warn("文件读取异常：{}", filename);
            return ResponseEntity.badRequest().build();
        }
    }

    public static void downloadServlet(HttpServletResponse response, File downloadFile) {
        try (OutputStream out = response.getOutputStream();
             InputStream in = Files.newInputStream(downloadFile.toPath())) {
            new DataWriter(out).write(in);
        } catch (IOException e) {
            log.warn("文件读取异常：{}", downloadFile.getName());
            throw new UncheckedIOException("文件读取异常", e);
        }
    }
}
