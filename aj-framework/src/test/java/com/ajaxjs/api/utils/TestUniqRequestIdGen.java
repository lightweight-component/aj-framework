package com.ajaxjs.api.utils;

import org.junit.jupiter.api.Test;

import static com.ajaxjs.api.utils.UniqRequestIdGen.*;

public class TestUniqRequestIdGen {
    @Test
    void test() {
        System.out.println(resolveReqId());

        testHexIpConversion("255.255.255.255", "FFFFFFFF");
        testHexIpConversion("192.168.1.1", "C0A80101");
        testHexIpConversion("10.0.0.1", "0A000001");
        System.out.println("All tests passed!");
    }
    private static void testHexIpConversion(String originalIp, String expectedHex) {
        String hex = hexIp(originalIp);
        System.out.println("Original IP: " + originalIp + ", Hex: " + hex);

        if (!hex.equalsIgnoreCase(expectedHex)) {
            throw new RuntimeException("Encoding failed for IP: " + originalIp);
        }

        String decodedIp = decodeHexIp(hex);
        System.out.println("Decoded IP: " + decodedIp);

        if (!decodedIp.equals(originalIp)) {
            throw new RuntimeException("Decoding failed for Hex: " + expectedHex);
        }
    }
    @Test
    void testGetInfo() {
        getInfo(resolveReqId());
    }
}
