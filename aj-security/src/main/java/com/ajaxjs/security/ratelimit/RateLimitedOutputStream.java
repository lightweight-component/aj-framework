package com.ajaxjs.security.ratelimit;

import com.ajaxjs.security.ratelimit.annotation.BandwidthUnit;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 限速输出流（支持分块写入）
 * <p>
 * 使用令牌桶算法控制写入速率，实现精确的带宽限速
 */
@Slf4j
public class RateLimitedOutputStream extends ServletOutputStream {

    /**
     * Stores the output stream value.
     */
    private final OutputStream outputStream;

    /**
     * Stores the token bucket value.
     */
    private final TokenBucket tokenBucket;

    /**
     * Stores the chunk size value.
     */
    private final int chunkSize;

    /**
     * Stores the bandwidth bytes per second value.
     */
    private final long bandwidthBytesPerSecond;

    /**
     * 统计信息
     */
    private long totalBytesWritten = 0;

    /**
     * Stores the start time value.
     */
    private final long startTime = System.nanoTime();

    /**
     * Stores the closed value.
     */
    private volatile boolean closed = false;

    /**
     * Stores the logged value.
     */
    private boolean logged = false;

    /**
     * Executes the rate limited output stream operation.
     *
     * @param outputStream            the output stream parameter.
     * @param bandwidthBytesPerSecond the bandwidth bytes per second parameter.
     */
    public RateLimitedOutputStream(OutputStream outputStream, long bandwidthBytesPerSecond) {
        this(outputStream, bandwidthBytesPerSecond, calculateOptimalChunkSize(bandwidthBytesPerSecond));
    }

    /**
     * 使用已有的 TokenBucket（共享限速状态）
     *
     * @param outputStream            底层输出流
     * @param tokenBucket             共享的令牌桶
     * @param bandwidthBytesPerSecond 限速（字节/秒）
     */
    public RateLimitedOutputStream(OutputStream outputStream, TokenBucket tokenBucket, long bandwidthBytesPerSecond) {
        this(outputStream, tokenBucket, bandwidthBytesPerSecond, calculateOptimalChunkSize(bandwidthBytesPerSecond));
    }

    /**
     * 使用已有的 TokenBucket（共享限速状态），指定分块大小
     *
     * @param outputStream            底层输出流
     * @param tokenBucket             共享的令牌桶
     * @param bandwidthBytesPerSecond 限速（字节/秒）
     * @param chunkSize               分块大小
     */
    public RateLimitedOutputStream(OutputStream outputStream, TokenBucket tokenBucket, long bandwidthBytesPerSecond, int chunkSize) {
        this.outputStream = outputStream;
        this.bandwidthBytesPerSecond = bandwidthBytesPerSecond;
        this.chunkSize = Math.max(512, Math.min(chunkSize, 65536));
        this.tokenBucket = tokenBucket;

        log.info("RateLimitedOutputStream created with shared bucket: bandwidth={}/s, chunkSize={}",
                BandwidthUnit.formatBytes(bandwidthBytesPerSecond), chunkSize);
    }

    /**
     * @param outputStream            底层输出流
     * @param bandwidthBytesPerSecond 限速（字节/秒）
     * @param chunkSize               分块大小，越小越平滑
     */
    public RateLimitedOutputStream(OutputStream outputStream, long bandwidthBytesPerSecond, int chunkSize) {
        this.outputStream = outputStream;
        this.bandwidthBytesPerSecond = bandwidthBytesPerSecond;
        this.chunkSize = Math.max(512, Math.min(chunkSize, 65536));

        long capacity = bandwidthBytesPerSecond;// 桶容量 = 1秒流量，允许短时突发
        this.tokenBucket = new TokenBucket(capacity, bandwidthBytesPerSecond);

        log.info("RateLimitedOutputStream created: bandwidth={}/s, chunkSize={}, capacity={}/s",
                BandwidthUnit.formatBytes(bandwidthBytesPerSecond), chunkSize, BandwidthUnit.formatBytes(capacity));
    }

    /**
     * 计算最佳分块大小
     * 经验公式：chunkSize = bandwidthBytesPerSecond / 50
     *
     * @param bandwidthBytesPerSecond the bandwidth bytes per second parameter.
     * @return the operation result.
     */
    static int calculateOptimalChunkSize(long bandwidthBytesPerSecond) {
        if (bandwidthBytesPerSecond < 200 * 1024)
            // 低于 200KB/s，使用 1-4KB
            return 1024;
        else if (bandwidthBytesPerSecond < 1024 * 1024)
            // 200KB/s - 1MB/s，使用 4-8KB
            return 4096;
        else if (bandwidthBytesPerSecond < 5 * 1024 * 1024)
            // 1MB/s - 5MB/s，使用 8-16KB
            return 8192;
        else
            // 高于 5MB/s，使用 16-32KB
            return 16384;
    }

    /**
     * Executes the write operation.
     *
     * @param b the b parameter.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void write(int b) throws IOException {
        checkClosed();
        tokenBucket.acquire(1);
        outputStream.write(b);
        totalBytesWritten++;
    }

    /**
     * Executes the write operation.
     *
     * @param b the b parameter.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Executes the write operation.
     *
     * @param b   the b parameter.
     * @param off the off parameter.
     * @param len the len parameter.
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkClosed();
        if (len == 0)
            return;

        if (!logged) {
            log.info("RateLimitedOutputStream.write() called with len={} bytes", len);
            logged = true;
        }

        int remaining = len;   // 分块写入，使流量更平滑
        int offset = off;

        while (remaining > 0) {
            int size = Math.min(chunkSize, remaining);
            tokenBucket.acquire(size);
            outputStream.write(b, offset, size);
            offset += size;
            remaining -= size;
            totalBytesWritten += size;
        }

        if (totalBytesWritten % (1024 * 1024) == 0) {
            double elapsed = (System.nanoTime() - startTime) / 1_000_000_000.0;
            double rate = elapsed > 0 ? (totalBytesWritten / elapsed) / 1024.0 : 0;
            log.info("Written {} bytes, actual rate: {} KB/s", totalBytesWritten, String.format("%.2f", rate));
        }
    }

    /**
     * Executes the flush operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void flush() throws IOException {
        checkClosed();
        outputStream.flush();
    }

    /**
     * Executes the close operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            double elapsed = (System.nanoTime() - startTime) / 1_000_000_000.0;
            double rate = elapsed > 0 ? (totalBytesWritten / elapsed) / 1024.0 : 0;
            log.info("RateLimitedOutputStream closing: total bytes={}, elapsed={}s, rate={} KB/s",
                    totalBytesWritten, String.format("%.2f", elapsed), String.format("%.2f", rate));
            outputStream.flush();
            outputStream.close();
        }
    }

    /**
     * Executes the check closed operation.
     *
     * @throws IOException if the operation cannot be completed.
     */
    void checkClosed() throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
    }

    /**
     * Executes the is ready operation.
     *
     * @return the operation result.
     */
    @Override
    public boolean isReady() {
        return !closed;
    }

    /**
     * Executes the set write listener operation.
     *
     * @param writeListener the write listener parameter.
     */
    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException("Async write not supported");
    }

    /**
     * 动态调整带宽
     *
     * @param newBandwidth the new bandwidth parameter.
     */
    public void setBandwidth(long newBandwidth) {
        tokenBucket.setRefillRate(newBandwidth);
    }

    /**
     * 获取当前可用令牌
     *
     * @return the operation result.
     */
    public long getAvailableTokens() {
        return tokenBucket.getAvailableTokens();
    }

    /**
     * 获取实际传输速率
     *
     * @return the operation result.
     */
    public double getActualRate() {
        long elapsedNanos = System.nanoTime() - startTime;
        if (elapsedNanos <= 0)
            return 0;

        long elapsedSeconds = elapsedNanos / 1_000_000_000L;
        return elapsedSeconds > 0 ? (double) totalBytesWritten / elapsedSeconds : 0;
    }

    /**
     * 获取总写入字节数
     *
     * @return the operation result.
     */
    public long getTotalBytesWritten() {
        return totalBytesWritten;
    }

    /**
     * 获取配置的带宽
     *
     * @return the operation result.
     */
    public long getBandwidthBytesPerSecond() {
        return bandwidthBytesPerSecond;
    }

    /**
     * 获取分块大小
     *
     * @return the operation result.
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * 获取令牌桶利用率
     *
     * @return the operation result.
     */
    public double getBucketUtilization() {
        return tokenBucket.getUtilization();
    }

    /**
     * Executes the get token bucket operation.
     *
     * @return the operation result.
     */
    public TokenBucket getTokenBucket() {
        return tokenBucket;
    }
}
