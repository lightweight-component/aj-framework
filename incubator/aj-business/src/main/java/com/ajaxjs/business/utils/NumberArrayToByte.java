package com.ajaxjs.business.utils;


import java.nio.*;

/**
 * bytes[]转long的三种方式
 *
 * <a href="https://blog.csdn.net/10km/article/details/77435659">...</a>
 */
public class NumberArrayToByte {
    public static double[] asDoubleArray(byte[] input) {
        if (null == input)
            return null;

        DoubleBuffer buffer = ByteBuffer.wrap(input).asDoubleBuffer();
        double[] res = new double[buffer.remaining()];
        buffer.get(res);

        return res;
    }

    public static float[] asFloatArray(byte[] input) {
        if (null == input)
            return null;

        FloatBuffer buffer = ByteBuffer.wrap(input).asFloatBuffer();
        float[] res = new float[buffer.remaining()];
        buffer.get(res);

        return res;
    }

    public static int[] asIntArray(byte[] input) {
        if (null == input)
            return null;

        IntBuffer buffer = ByteBuffer.wrap(input).asIntBuffer();
        int[] res = new int[buffer.remaining()];
        buffer.get(res);

        return res;
    }

    public static long[] asLongArray(byte[] input) {
        if (null == input)
            return null;

        LongBuffer buffer = ByteBuffer.wrap(input).asLongBuffer();
        long[] res = new long[buffer.remaining()];
        buffer.get(res);

        return res;
    }

    public static short[] asShortArray(byte[] input) {
        if (null == input)
            return null;

        ShortBuffer buffer = ByteBuffer.wrap(input).asShortBuffer();
        short[] res = new short[buffer.remaining()];
        buffer.get(res);

        return res;
    }

    /*
     * 从多字节类型数组(double[],float[],long[]…)转byte[]要稍麻烦一些，
     * 因为多字节类型数组对应的Buffer类并没提供asByteBuffer这样的方法.
     * 所以要自己写些代码进行转换(比如将DoubleBuffer转为ByteBuffer)
     */

    /**
     * {@link DoubleBuffer} TO {@link ByteBuffer}
     *
     * @param input
     * @return
     */
    public static ByteBuffer asByteBuffer(DoubleBuffer input) {
        if (null == input)
            return null;

        ByteBuffer buffer = ByteBuffer.allocate(input.capacity() * (Double.SIZE / 8));
        while (input.hasRemaining())
            buffer.putDouble(input.get());

        return buffer;
    }

    /**
     * double[] TO byte[]
     *
     * @param input
     * @return
     */
    public static byte[] asByteArray(double[] input) {
        if (null == input)
            return null;

        return asByteBuffer(DoubleBuffer.wrap(input)).array();
    }

    /**
     * {@link FloatBuffer} TO {@link ByteBuffer}
     *
     * @param input
     * @return
     */
    public static ByteBuffer asByteBuffer(FloatBuffer input) {
        if (null == input)
            return null;

        ByteBuffer buffer = ByteBuffer.allocate(input.capacity() * (Float.SIZE / 8));
        while (input.hasRemaining())
            buffer.putFloat(input.get());

        return buffer;
    }

    /**
     * float[] TO byte[]
     *
     * @param input
     * @return
     */
    public static byte[] asByteArray(float[] input) {
        if (null == input)
            return null;

        return asByteBuffer(FloatBuffer.wrap(input)).array();
    }

    /**
     * {@link IntBuffer} TO {@link ByteBuffer}
     *
     * @param input
     * @return
     */
    public static ByteBuffer asByteBuffer(IntBuffer input) {
        if (null == input)
            return null;

        ByteBuffer buffer = ByteBuffer.allocate(input.capacity() * (Integer.SIZE / 8));
        while (input.hasRemaining())
            buffer.putInt(input.get());

        return buffer;
    }

    /**
     * int[] TO byte[]
     *
     * @param input
     * @return
     */
    public static byte[] asByteArray(int[] input) {
        if (null == input)
            return null;

        return asByteBuffer(IntBuffer.wrap(input)).array();
    }

    /**
     * {@link LongBuffer} TO {@link ByteBuffer}
     *
     * @param input
     * @return
     */
    public static ByteBuffer asByteBuffer(LongBuffer input) {
        if (null == input)
            return null;

        ByteBuffer buffer = ByteBuffer.allocate(input.capacity() * (Long.SIZE / 8));
        while (input.hasRemaining())
            buffer.putLong(input.get());

        return buffer;
    }

    /**
     * long[] TO byte[]
     *
     * @param input
     * @return
     */
    public static byte[] asByteArray(long[] input) {
        if (null == input)
            return null;

        return asByteBuffer(LongBuffer.wrap(input)).array();
    }

    /**
     * {@link ShortBuffer} TO {@link ByteBuffer}
     *
     * @param input
     * @return
     */
    public static ByteBuffer asByteBuffer(ShortBuffer input) {
        if (null == input)
            return null;

        ByteBuffer buffer = ByteBuffer.allocate(input.capacity() * (Short.SIZE / 8));
        while (input.hasRemaining())
            buffer.putShort(input.get());

        return buffer;
    }

    /**
     * short[] TO byte[]
     *
     * @param input
     * @return
     */
    public static byte[] asByteArray(short[] input) {
        if (null == input)
            return null;

        return asByteBuffer(ShortBuffer.wrap(input)).array();
    }

    /**
     * 将字节数组转为long<br>
     * 如果input为null,或offset指定的剩余数组长度不足8字节则抛出异常
     *
     * @param input
     * @param offset       起始偏移量
     * @param littleEndian 输入数组是否小端模式
     * @return
     */
    public static long longFrom8Bytes(byte[] input, int offset, boolean littleEndian) {
        long value = 0;
        // 循环读取每个字节通过移位运算完成long的8个字节拼装
        for (int count = 0; count < 8; ++count) {
            int shift = (littleEndian ? count : (7 - count)) << 3;
            value |= ((long) 0xff << shift) & ((long) input[offset + count] << shift);
        }

        return value;
    }

    /**
     * 利用 {@link java.nio.ByteBuffer}实现byte[]转long
     *
     * @param input
     * @param offset       起始偏移量
     * @param littleEndian 输入数组是否小端模式
     * @return
     */
    public static long bytesToLong(byte[] input, int offset, boolean littleEndian) {
        // 将byte[] 封装为 ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(input, offset, 8);

        if (littleEndian) {
            // ByteBuffer.order(ByteOrder) 方法指定字节序,即大小端模式(BIG_ENDIAN/LITTLE_ENDIAN)
            // ByteBuffer 默认为大端(BIG_ENDIAN)模式
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        return buffer.getLong();
    }

}
