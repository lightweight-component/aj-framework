package com.ajaxjs.fileupload.magicnumber;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link MagicNumber#readPrefix(InputStream, int)}.
 */
class TestMagicNumberReadPrefix {

    /**
     * Verifies that {@code readPrefix} reads exactly the requested number of bytes
     * when the stream contains at least that many bytes.
     */
    @Test
    void readPrefixExactBytes() throws IOException {
        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) i;

        byte[] result = MagicNumber.readPrefix(new ByteArrayInputStream(data), 100);

        assertArrayEquals(data, result);
    }

    /**
     * Verifies that {@code readPrefix} returns fewer bytes than the limit
     * when the stream is shorter than the requested limit.
     */
    @Test
    void readPrefixStreamShorterThanLimit() throws IOException {
        byte[] data = new byte[50];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) (i + 10);

        byte[] result = MagicNumber.readPrefix(new ByteArrayInputStream(data), 100);

        assertEquals(50, result.length);
        assertArrayEquals(data, result);
    }

    /**
     * Verifies that {@code readPrefix} returns an empty array when the
     * input stream is empty.
     */
    @Test
    void readPrefixEmptyStream() throws IOException {
        byte[] result = MagicNumber.readPrefix(new ByteArrayInputStream(new byte[0]), 100);

        assertEquals(0, result.length);
    }

    /**
     * Verifies that {@code readPrefix} reads all available bytes when the
     * limit is larger than the stream content.
     */
    @Test
    void readPrefixLimitLargerThanStream() throws IOException {
        byte[] data = new byte[200];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) (i + 50);

        byte[] result = MagicNumber.readPrefix(new ByteArrayInputStream(data), 500);

        assertEquals(200, result.length);
        assertArrayEquals(data, result);
    }
}