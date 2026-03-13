package util;


import com.ajaxjs.util.BytesHelper;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.ajaxjs.util.BytesHelper.bytesToHexStr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestBytesHelper {
    /**
     * 在字节数组里查找某个字节数组，找到返回&lt;=0，未找到返回-1
     *
     * @param data   被搜索的内容
     * @param search 要搜索内容
     * @param start  搜索起始位置
     * @return 目标位置，找不到返回-1
     */
    public static int byteIndexOf(byte[] data, byte[] search, int start) {
        int len = search.length;

        for (int i = start; i < data.length; i++) {
            int temp = i, j = 0;

            while (data[temp] == search[j]) {
                temp++;
                j++;

                if (j == len)
                    return i;
            }
        }

        return -1;
    }

    /**
     * 在字节数组里查找某个字节数组，找到返回 &lt;=0，未找到返回 -1
     *
     * @param data   被搜索的内容
     * @param search 要搜索内容
     * @return 目标位置，找不到返回 -1
     */
    public static int byteIndexOf(byte[] data, byte[] search) {
        return byteIndexOf(data, search, 0);
    }

    /**
     * 在字节数组中截取指定长度数组
     *
     * @param data   输入的数据
     * @param off    偏移
     * @param length 长度
     * @return 指定 范围的字节数组
     */
    public static byte[] subBytes(byte[] data, int off, int length) {
        byte[] bs = new byte[length];
        System.arraycopy(data, off, bs, 0, length);

        return bs;
    }

    /**
     * char 数组转 byte 数组
     * 将 char 数组转换为 byte 数组需要考虑编码方式的问题
     * <a href="https://houbb.github.io/2023/06/05/java-perf-02-chars-to-bytes">...</a>
     *
     * @param chars 输入的字符数组。
     * @return 转换后的字节数组。
     */
    public static byte[] charToByte(char[] chars) {
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);

        return bytes;
    }

    @Test
    void testParseHexStr2Byte() {
        byte[] bs = BytesHelper.parseHexStr2Byte("1A2B3C");
        assert bs != null;
        assertEquals(0x1A, bs[0]);
    }

    /**
     * Test case: Standard byte array input.
     * Verifies that the method correctly converts a standard byte array to its hexadecimal string representation.
     */
    @Test
    void testBytesToHexStr_StandardCase() {
        byte[] input = new byte[]{0x1A, 0x2B, 0x3C};
        String result = bytesToHexStr(input);
        assertEquals("1A2B3C", result);
    }

    /**
     * Test case: All zero bytes.
     * Verifies that the method correctly converts an array of zero bytes to the appropriate hexadecimal string.
     */
    @Test
    void testBytesToHexStr_AllZeros() {
        byte[] input = new byte[]{0x00, 0x00, 0x00};
        String result = bytesToHexStr(input);
        assertEquals("000000", result);
    }

    /**
     * Test case: Bytes with high bit set.
     * Verifies that the method correctly handles bytes where the high bit is set (negative values in Java).
     */
    @Test
    void testBytesToHexStr_HighBitSet() {
        byte[] input = new byte[]{(byte) 0x80, (byte) 0xFF};
        String result = bytesToHexStr(input);
        assertEquals("80FF", result);
    }

    /**
     * Test case: Empty byte array.
     * Verifies that the method returns an empty string when given an empty byte array.
     */
    @Test
    void testBytesToHexStr_EmptyArray() {
        byte[] input = new byte[0];
        String result = bytesToHexStr(input);
        assertEquals("", result);
    }

    /**
     * Test case: Single byte values.
     * Verifies several specific single byte conversions.
     */
    @Test
    void testBytesToHexStr_SingleBytes() {
        assertEquals("00", bytesToHexStr(new byte[]{0x00}));
        assertEquals("FF", bytesToHexStr(new byte[]{(byte) 0xFF}));
        assertEquals("7F", bytesToHexStr(new byte[]{0x7F}));
        assertEquals("80", bytesToHexStr(new byte[]{(byte) 0x80}));
    }

    /**
     * Test case: Long byte array.
     * Verifies that the method correctly converts a longer byte array without errors.
     */
    @Test
    void testBytesToHexStr_LongArray() {
        byte[] input = new byte[100];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) i;
        }

        String result = bytesToHexStr(input);
        assertEquals(200, result.length()); // Each byte should produce 2 hex chars

        // Verify first few values
        assertTrue(result.startsWith("000102030405"));
    }
}
