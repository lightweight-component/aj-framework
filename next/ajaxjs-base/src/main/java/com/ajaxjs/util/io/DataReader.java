package com.ajaxjs.util.io;

import com.ajaxjs.util.CommonConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The reader class for reading data from an input stream.
 * InputStream is used for reading raw byte data from a source like a file, network socket, or memory buffer.
 * If its text is not binary data, Reader can be used.
 * If it does not meet the need, you can refer to the Spring StreamUtils/ResourceUtils/FileCopyUtils/FileSystemUtils.
 */
@Slf4j
@RequiredArgsConstructor
public class DataReader {
    /**
     * The input stream to read data from.
     */
    private final InputStream in;

    /**
     * The character encoding to use when reading the input stream. UTF-8 is used by default.
     */
    private Charset encode = StandardCharsets.UTF_8;

    /**
     * Create a DataReader instance with the specified input stream and character encoding.
     *
     * @param in     The input stream to read data from.
     * @param encode The character encoding to use when reading the input stream.
     */
    public DataReader(InputStream in, Charset encode) {
        this(in);
        this.encode = encode;
    }

    /*
     * 两端速度不匹配，需要协调 理想环境下，速度一样快，那就没必要搞流，直接一坨给弄过去就可以了
     * 但这是理想情况下，实际要考虑内存占用。
     * 于是采用流的概念来优化。流的意思很形象，就是一点一滴的，不是一坨坨大批量的
     * Explanation of streaming concept:
     * In ideal situations where both end process data at the same speed, streaming wouldn't be necessary,
     * and we could transfer data in bulk.
     * However, in real-world scenarios, we need to consider memory usage.
     * Streaming optimizes this by processing data incrementally rather than all at once.
     */

    /**
     * Read the binary data from the input stream as bytes.
     * It will close the input stream when the reading process is complete.
     *
     * @param bufferSize The template buffer size.
     * @param fn         What do you want to do with the data?
     */
    public void readStreamAsBytes(int bufferSize, BiConsumer<Integer, byte[]> fn) {
        int readSize; // 读取到的数据长度
        byte[] buffer = new byte[bufferSize]; // 通过 byte 作为数据中转，用于存放循环读取的临时数据

        try {
            while ((readSize = in.read(buffer)) != -1)
                fn.accept(readSize, buffer);
        } catch (IOException e) {
            log.warn("Error occurred when reading the stream data to bytes.", e);
            throw new UncheckedIOException("Error occurred when reading the stream data to bytes.", e);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                log.warn("Error occurred when closing the input stream.", e);
            }
        }
    }

    /**
     * Read the text data from the input stream as line by line.
     *
     * @param fn provided function to consume each line of data
     */
    public void readAsLineString(Consumer<String> fn) {
        /*
         * 装饰器模式，又称为包装器，可以在不修改被包装类的情况下动态添加功能（例如缓冲区功能）
         * 这里使用 BufferedReader 为输入流添加缓冲功能
         */
        try (InputStreamReader inReader = new InputStreamReader(in, encode);
             BufferedReader reader = new BufferedReader(inReader)) {
            String line;

            while ((line = reader.readLine()) != null) // 一次读入一行，直到读入 null 表示文件结束
                fn.accept(line);// 指定编码集的另外一种写法 line = new String(line.getBytes(), encodingSet);
        } catch (IOException e) {
            log.warn("Error occurred when reading the data from stream.", e);
            throw new UncheckedIOException("Error occurred when reading the data from stream.", e);
        }
    }

    /**
     * Read the data from the input stream as a string.
     *
     * @return String
     */
    public String readAsString() {
        StringBuffer result = new StringBuffer();

        readAsLineString(line -> {
            result.append(line);
            result.append(CommonConstant.NEW_LINE);
        });

        return result.toString();
    }

    /**
     * Read the data from the input stream as bytes.
     * This equivalent to using the DataWriter class to write the data to a ByteArrayOutputStream, that means the data is stored in memory.
     *
     * @return The bytes in RAM.
     */
    public byte[] readAsBytes() {// 等价于 Spring StreamUtils.copyToByteArray(InputStream in)：将输入流的内容复制到字节数组
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            new DataWriter(out).write(in);

            return out.toByteArray();
        } catch (IOException e) {
            log.warn("Error occurred when reading the data from stream as bytes.", e);
            throw new UncheckedIOException("Error occurred when reading the data from stream as bytes.", e);
        }
    }
}