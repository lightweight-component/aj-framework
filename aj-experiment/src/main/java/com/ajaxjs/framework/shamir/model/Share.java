package com.ajaxjs.framework.shamir.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * 密钥份额
 */
@Data
@AllArgsConstructor
public class Share {
    /**
     * 份额的 x 坐标
     */
    private final int x;

    /**
     * 份额的 y 坐标
     */
    private final BigInteger y;

    /**
     * 编码为 Base64 字符串，格式：x:y(hex)
     */
    public String encode() {
        return x + ":" + y.toString(16);
    }

    /**
     * 从编码字符串解码
     */
    public static Share decode(String encoded) {
        String[] parts = encoded.split(":");

        if (parts.length != 2)
            throw new IllegalArgumentException("Invalid share format");

        int x = Integer.parseInt(parts[0]);
        BigInteger y = new BigInteger(parts[1], 16);

        return new Share(x, y);
    }

    @Override
    public String toString() {
        return "Share{x=" + x + ", y=" + y.toString(16) + "}";
    }
}