package com.ajaxjs.security.ratelimit;

import com.ajaxjs.security.ratelimit.annotation.BandwidthUnit;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * 带宽限速响应包装器
 * <p>
 * 包装 HttpServletResponse 的 OutputStream，使用 RateLimitedOutputStream 实现限速
 */
@Slf4j
public class BandwidthLimitResponseWrapper extends HttpServletResponseWrapper {
    private final long bandwidthBytesPerSecond;
    private final int chunkSize;
    private final TokenBucket sharedTokenBucket;
    private RateLimitedOutputStream limitedOutputStream;
    private PrintWriter writer;
    private boolean outputStreamUsed = false;
    private boolean headersCopied = false;

    public BandwidthLimitResponseWrapper(HttpServletResponse response, long bandwidthBytesPerSecond) {
        this(response, null, bandwidthBytesPerSecond, -1);
    }

    public BandwidthLimitResponseWrapper(HttpServletResponse response, long bandwidthBytesPerSecond, int chunkSize) {
        this(response, null, bandwidthBytesPerSecond, chunkSize);
    }

    public BandwidthLimitResponseWrapper(HttpServletResponse response,
                                         TokenBucket tokenBucket,
                                         long bandwidthBytesPerSecond,
                                         int chunkSize) {
        super(response);
        this.sharedTokenBucket = tokenBucket;
        this.bandwidthBytesPerSecond = bandwidthBytesPerSecond;
        this.chunkSize = chunkSize;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (!outputStreamUsed) {
            log.info("BandwidthLimitResponseWrapper.getOutputStream() called, bandwidth={}/s, sharedBucket={}",
                    BandwidthUnit.formatBytes(bandwidthBytesPerSecond), sharedTokenBucket != null);
            outputStreamUsed = true;
        }

        if (limitedOutputStream == null) {
            ServletOutputStream out = super.getOutputStream();

            if (sharedTokenBucket != null) {
                if (chunkSize > 0) // 使用共享的 TokenBucket
                    limitedOutputStream = new RateLimitedOutputStream(out, sharedTokenBucket, bandwidthBytesPerSecond, chunkSize);
                else
                    limitedOutputStream = new RateLimitedOutputStream(out, sharedTokenBucket, bandwidthBytesPerSecond);
            } else {
                if (chunkSize > 0) // 创建新的 TokenBucket（兼容旧代码）
                    limitedOutputStream = new RateLimitedOutputStream(out, bandwidthBytesPerSecond, chunkSize);
                else
                    limitedOutputStream = new RateLimitedOutputStream(out, bandwidthBytesPerSecond);
            }
        }

        return limitedOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null)
            writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), getCharacterEncoding()), true);

        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null)
            writer.flush();
        else if (limitedOutputStream != null)
            limitedOutputStream.flush();

        super.flushBuffer();
    }

    @Override
    public void setContentType(String type) {
        super.setContentType(type);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        super.setCharacterEncoding(charset);
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
    }

    /**
     * 获取限速输出流（用于获取统计信息）
     */
    public RateLimitedOutputStream getRateLimitedOutputStream() {
        return limitedOutputStream;
    }

    public void close() throws IOException {
        if (limitedOutputStream != null) {
            log.info("BandwidthLimitResponseWrapper closing, total bytes: {}",
                    limitedOutputStream.getTotalBytesWritten());
            limitedOutputStream.close();
        }
    }
}