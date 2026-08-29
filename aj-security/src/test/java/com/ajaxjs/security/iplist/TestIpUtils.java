package com.ajaxjs.security.iplist;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Represents the ip utils test component.
 */
class TestIpUtils {
    @Test
    void testGetClientRealIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        // 场景1：无代理，直接访问
        request.setRemoteAddr("123.45.67.89");
        assertEquals("123.45.67.89", IpUtils.getClientRealIp(request));

        // 场景2：单代理，X-Forwarded-For 包含真实 IP
        request.addHeader("X-Forwarded-For", "123.45.67.89");
        request.setRemoteAddr("10.0.0.1"); // 代理 IP
        assertEquals("123.45.67.89", IpUtils.getClientRealIp(request));

        // 场景3：多级代理，X-Forwarded-For 包含多个 IP
        request.addHeader("X-Forwarded-For", "123.45.67.89, 10.0.1.100, 10.0.1.101");
        assertEquals("123.45.67.89", IpUtils.getClientRealIp(request));

        // 场景4：IPv6地址
        request.addHeader("X-Forwarded-For", "2001:db8::1");
        assertEquals("2001:db8::1", IpUtils.getClientRealIp(request));
    }

    @Test
    void testPackageVisibleIpHelpers() {
        assertEquals("203.0.113.10", IpUtils.parseXForwardedFor("unknown, 10.0.0.1, 203.0.113.10"));
        assertNull(IpUtils.parseXForwardedFor("unknown, 10.0.0.1"));
        assertTrue(IpUtils.isValidIp("2001:db8::1"));
        assertFalse(IpUtils.isValidIp("not-an-ip"));
        assertTrue(IpUtils.isInternalIp("192.168.1.1"));
        assertFalse(IpUtils.isValidPublicIp("127.0.0.1"));
        assertTrue(IpUtils.isLocalhost("0:0:0:0:0:0:0:1"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "203.0.113.11");
        assertEquals("203.0.113.11", IpUtils.getIpFromHeaders(request));
    }
}
