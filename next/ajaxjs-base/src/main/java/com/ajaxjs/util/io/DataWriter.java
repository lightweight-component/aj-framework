package com.ajaxjs.util.io;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * The writer class for writing data to an output stream.
 * OutputStream is used for writing raw byte data to a destination like a file, socket, or buffer.
 * If it does not meet the need, you can refer to the Spring StreamUtils/ResourceUtils/FileCopyUtils/FileSystemUtils.
 * Please note that this class does not close the output stream.
 */
@Slf4j
@RequiredArgsConstructor
@Data
public class DataWriter {
    /**
     * The output stream to write data to.
     * Please note that this class does not close the output stream.
     */
    private final OutputStream out;

    /**
     * 1K 的数据块
     */
    public static final int BUFFER_SIZE = 1024;

    /**
     * Add a buffer to the output stream. It's true by default.
     */
    private boolean isBuffered = true;

    /**
     * Write data from an input stream to an output stream.
     * Please note that the output stream is not closed by this method.
     *
     * @param in InputStream, please note that the output stream is not closed by this method.
     */
    public void write(InputStream in) {
        if (!isBuffered)
            log.warn("It's not recommended that NOT using BufferedOutputStream.");

        try {
            OutputStream _out = isBuffered ? new BufferedOutputStream(out) : out;// 加入缓冲功能

            new DataReader(in).readStreamAsBytes(BUFFER_SIZE, (readSize, buffer) -> {
                try {
                    _out.write(buffer, 0, readSize);
                } catch (IOException e) {
                    log.warn("Error occurred when copying input to output data.", e);
                    throw new UncheckedIOException("Error occurred when copying input to output data.", e);
                }
            });

            _out.flush();
        } catch (IOException e) {
            log.warn("Error occurred when copying input to output data.", e);
            throw new UncheckedIOException("Error occurred when copying input to output data.", e);
        }
    }

    /**
     * Input bytes to output stream.
     * You can specify a part of the byte[] data.
     *
     * @param data   Input data
     * @param off    Offset of the byte[] data, it can be zero
     * @param length The length of the byte[] data, it can be zero
     */
    public void write(byte[] data, int off, int length) {
        try {
            OutputStream bOut = isBuffered ? new BufferedOutputStream(out, BUFFER_SIZE) : out;

            if (off == 0 && length == 0)
                bOut.write(data);
            else
                bOut.write(data, off, length);

            bOut.flush();
        } catch (IOException e) {
            log.warn("Error occurred when writing bytes to a OutputStream.", e);
            throw new UncheckedIOException("Error occurred when writing bytes to a OutputStream.", e);
        }
    }

    /**
     * Input all the bytes to output stream.
     *
     * @param data Input data
     */
    public void write(byte[] data) {
        write(data, 0, 0);
    }

    /*
     * 输入流复制到输出流
     * New JDK 9 新特性，直接复制流，不用自己写循环了
     *
     * @param in 输入流
     * @return 复制了多少字节

    public long write(InputStream in) {
        try {
            return in.transferTo(out); // 等价 Spring StreamUtils.copy(in, out)
        } catch (IOException e) {
            throw new RuntimeException("复制流的时候失败", e);
        }
    }
    */
}