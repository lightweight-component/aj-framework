package com.ajaxjs.util.io;

import com.ajaxjs.util.io.DataWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestDataWriter {
    private ByteArrayOutputStream mockOut;
    private ByteArrayOutputStream noBufferMockOut;
    private DataWriter writerWithBuffer;
    private DataWriter writerWithoutBuffer;

    @BeforeEach
    void setUp() {
        mockOut = new ByteArrayOutputStream();
        writerWithBuffer = new DataWriter(mockOut);
        writerWithBuffer.setBuffered(true);

        noBufferMockOut = new ByteArrayOutputStream();
        writerWithoutBuffer = new DataWriter(noBufferMockOut);
        writerWithoutBuffer.setBuffered(false);
    }

    static final String testData = "Hello World!";
    static final byte[] testBytes = testData.getBytes(StandardCharsets.UTF_8);

    @Test
    void testWrite_NoBuffer() {// With buffer is already in the reader.readAsBytes()
        InputStream in = new ByteArrayInputStream(testBytes);
        writerWithoutBuffer.write(in);

        assertEquals(testData, new String(noBufferMockOut.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    void testWriteBytes() {
        writerWithBuffer.write(testBytes);

        assertArrayEquals(testBytes, mockOut.toByteArray());
        assertEquals(testData, new String(mockOut.toByteArray(), StandardCharsets.UTF_8));

        writerWithoutBuffer.write(testBytes);

        assertArrayEquals(testBytes, noBufferMockOut.toByteArray());
        assertEquals(testData, new String(noBufferMockOut.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    void testWriteBytesNoBuffer() {
        byte[] testBytes = "PartialArray".getBytes(StandardCharsets.UTF_8);

        writerWithBuffer.write(testBytes, 7, 5);
        assertEquals("Array", new String(mockOut.toByteArray(), StandardCharsets.UTF_8));

        writerWithoutBuffer.write(testBytes, 7, 5);
        assertEquals("Array", new String(noBufferMockOut.toByteArray(), StandardCharsets.UTF_8));
    }
}
