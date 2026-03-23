package com.ajaxjs.util.cryptography;

import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;

public class TestCertificateUtils {
    @Test
    void testGetCert() {
        X509Certificate cert = CertificateUtils.getCert(
                "D:\\sp42\\code\\ajaxjs\\aj-util\\src\\test\\java\\com\\ajaxjs\\util\\cryptography\\1623777099_20251021_cert\\apiclient_cert.pem");
        System.out.println(cert);
    }
}
