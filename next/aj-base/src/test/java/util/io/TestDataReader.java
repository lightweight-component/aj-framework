package util.io;

import com.ajaxjs.util.io.DataReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestDataReader {
    private InputStream inputStream;

    private String testString;

    @BeforeEach
    public void setUp() {
        testString = "测试字符串";
        inputStream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testReadAsString() {
        String result = new DataReader(inputStream).readAsString();
        assertEquals(testString + "\n", result);
    }

    @Test
    public void testReadAsBytes() {
        byte[] result = new DataReader(inputStream).readAsBytes();
        assertEquals(testString, new String(result, StandardCharsets.UTF_8));
    }
}
