package com.ajaxjs.business.net.netcard;

/**
 * 基数，指的是数字系统的进制
 */
public enum Radix {
    /**
     * 二进制
     */
    BIN(2),
    /**
     * 十进制
     */
    DEC(10),
    /**
     * 十六进制
     */
    HEX(16),
    /**
     * 十六进制2字节
     */
    HEX16(16);

    public final int value;

    Radix(int radix) {
        this.value = radix;
    }
}
