package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.cryptography.rsa.KeyMgr;
import org.junit.jupiter.api.Test;

import java.security.Key;

/**
 * Private key utils
 */
public class TestKeyMgr {
    @Test
    void testRestore() {
        Key key = KeyMgr.loadPrivateKey("C:\\Users\\Z\\Downloads\\WXCertUtil\\cert\\1623777099_20251021_cert\\apiclient_key.pem");

        System.out.println(key.getAlgorithm());
    }
}
