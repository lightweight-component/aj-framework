package com.ajaxjs.business.net.netcard;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AddressHelper {
    public static final String DEFAULT_HOST = "localhost";

    private static final byte[][] INVALID_MACS = {{0x00, 0x05, 0x69}, // VMWare
            {0x00, 0x1C, 0x14}, // VMWare
            {0x00, 0x0C, 0x29}, // VMWare
            {0x00, 0x50, 0x56}, // VMWare
            {0x08, 0x00, 0x27}, // Virtualbox
            {0x0A, 0x00, 0x27}, // Virtualbox
            {0x00, 0x03, (byte) 0xFF}, // Virtual-PC
            {0x00, 0x15, 0x5D} // Hyper-V
    };

    /**
     * 检查给定的 MAC 地址是否为虚拟机的 MAC 地址
     *
     * @param mac 待检查的 MAC 地址，为一个字节数组
     * @return 如果是虚拟机的MAC地址则返回 true，否则返回 false
     */
    public static boolean isVMMac(byte[] mac) {
        if (null == mac)
            return false;

        for (byte[] invalid : INVALID_MACS) {
            // 比较传入的MAC地址与无效MAC地址的前三个字节
            if (invalid[0] == mac[0] && invalid[1] == mac[1] && invalid[2] == mac[2])
                // 如果前三个字节匹配，则表示这是虚拟机的MAC地址，返回true
                return true;
        }

        // 如果没有与任何无效MAC地址的前三个字节匹配，则不是虚拟机的MAC地址，返回false
        return false;
    }

    /**
     * 判断 {@code host} 是否为 localhost
     */
    public static final boolean isLoopbackAddress(String host) {
        return "127.0.0.1".equals(host) || "::1".equals(host) || DEFAULT_HOST.equals(host);
    }

    /**
     * 判断{@code address}是否为本机地址
     */
    public static boolean isLocalhost(InetAddress address) {
        try {
            return address.isLoopbackAddress()
                    || InetAddress.getLocalHost().getHostAddress().equals(address.getHostAddress());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断{@code address}是否为本机地址
     */
    public static boolean isLocalhost(String host) {
        try {
            return isLoopbackAddress(host) || isLocalhost(InetAddress.getByName(host));
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 判断一个地址是否为广播地址(255.255.255.255)
     *
     * @param addr IP 地址对象
     * @return addr为广播地址返回{@code true}，否则返回{@code false}
     */
    public static boolean isBroadcast(InetAddress addr) {
        return addr.getHostAddress().equals("255.255.255.255");
    }

}
