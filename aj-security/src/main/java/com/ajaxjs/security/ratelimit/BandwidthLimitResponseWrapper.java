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
    /**
     * Stores the bandwidth bytes per second value.
     */
    private final long bandwidthBytesPerSecond;

    /**
     * Stores the chunk size value.
     */
    private final int chunkSize;

    /**
     * Stores the shared token bucket value.
     */
    private final TokenBucket sharedTokenBucket;

    /**
     * Stores the limited output stream value.
     */
    private RateLimitedOutputStream limitedOutputStream;

    /**
     * Stores the writer value.
     */
    private PrintWriter writer;

    /**
     * Stores the output stream used value.
     */
    private boolean outputStreamUsed = false;

    /**
     * Stores the headers copied value.
     */
    private boolean headersCopied = false;

    /**
     * Executes the bandwidth limit response wrapper operation.
     *
     * @param response                the response parameter.
     * @param bandwidthBytesPerSecond the bandwidth bytes per second parameter.
     */
    public BandwidthLimitResponseWrapper(HttpServletResponse response, long bandwidthBytesPerSecond) {
        this(response, null, bandwidthBytesPerSecond, -1);
    }

    /**
     * Executes the bandwidth limit response wrapper operation.
     *
     * @param response                the response parameter.
     * @param bandwidthBytesPerSecond the bandwidth bytes per second parameter.
     * @param chunkSize               the chunk size parameter.
     */
    public BandwidthLimitResponseWrapper(HttpServletResponse response, long bandwidthBytesPerSecond, int chunkSize) {
        this(response, null, bandwidthBytesPerSecond, chunkSize);
    }

    /**
     * Executes the bandwidth limit response wrapper operation.
     *
     * @param response                the response parameter.
     * @param tokenBucket             the token bucket parameter.
     * @param bandwidthBytesPerSecond the bandwidth bytes per second parameter.
     * @param chunkSize               the chunk size parameter.
     */
    public BandwidthLimitResponseWrapper(HttpServletResponse response,
                                         TokenBucket tokenBucket,
                                         long bandwidthBytesPerSecond,
                                         int chunkSize) {
        super(response);
        this.sharedTokenBucket = tokenBucket;
        this.bandwidthBytesPerSecond = bandwidthBytesPerSecond;
        this.chunkSize = chunkSize;
    }

    /**
     * Executes the get output stream operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
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

    /**
     * Executes the get writer operation.
     *
     * @return the operation result.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null)
            writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), getCharacterEncoding()), true);

        return writer;
    }

    /**
     * Executes the flush buffer operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void flushBuffer() throws IOException {
        if (writer != null)
            writer.flush();
        else if (limitedOutputStream != null)
            limitedOutputStream.flush();

        super.flushBuffer();
    }

    /**
     * Executes the set content type operation.
     *
     * @param type the type parameter.
     */
    @Override
    public void setContentType(String type) {
        super.setContentType(type);
    }

    /**
     * Executes the set character encoding operation.
     *
     * @param charset the charset parameter.
     */
    @Override
    public void setCharacterEncoding(String charset) {
        super.setCharacterEncoding(charset);
    }

    /**
     * Executes the set header operation.
     *
     * @param name  the name parameter.
     * @param value the value parameter.
     */
    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
    }

    /**
     * Executes the add header operation.
     *
     * @param name  the name parameter.
     * @param value the value parameter.
     */
    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
    }

    /**
     * Executes the set int header operation.
     *
     * @param name  the name parameter.
     * @param value the value parameter.
     */
    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
    }

    /**
     * 获取限速输出流（用于获取统计信息）
     *
     * @return the operation result.
     */
    public RateLimitedOutputStream getRateLimitedOutputStream() {
        return limitedOutputStream;
    }

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    public void close() throws IOException {
        if (limitedOutputStream != null) {
            log.info("BandwidthLimitResponseWrapper closing, total bytes: {}",
                    limitedOutputStream.getTotalBytesWritten());
            limitedOutputStream.close();
        }
    }
}
