package com.ajaxjs.spring.traceid;

import org.springframework.util.StreamUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 重复消费 RawBody 内容
 * 不支持 request 其他的方法，没有处理
 * <a href="https://blog.csdn.net/10km/article/details/140735079">...</a>
 */
//@Deprecated
public class BufferedRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] buffer;

    public BufferedRequestWrapper(HttpServletRequest request) {
        super(request);

        try (InputStream in = request.getInputStream()) { // 读取 InputSteam 中的数据保存到 buffer
  /*      ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int readCount;

        while ((readCount = is.read(buf)) > 0)
            os.write(buf, 0, readCount);

        buffer = os.toByteArray();*/

            buffer = StreamUtils.copyToByteArray(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        return new BufferedServletInputStream(buffer);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), UTF_8));
    }

}