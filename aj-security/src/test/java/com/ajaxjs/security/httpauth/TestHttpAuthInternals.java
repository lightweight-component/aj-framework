package com.ajaxjs.security.httpauth;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests package-visible HTTP authentication helpers.
 */
class TestHttpAuthInternals {
    @Test
    void testParseDigestHeader() {
        Map<String, String> values = HttpDigestAuth.parseDigestHeader(
                "Digest username=\"alice\", realm=\"api\", nonce=\"n1\", qop=auth");

        assertEquals("alice", values.get("username"));
        assertEquals("api", values.get("realm"));
        assertEquals("n1", values.get("nonce"));
        assertEquals("auth", values.get("qop"));
    }
}
