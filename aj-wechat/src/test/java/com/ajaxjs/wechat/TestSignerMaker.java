package com.ajaxjs.wechat;

import com.ajaxjs.util.cryptography.rsa.DoSignature;
import com.ajaxjs.wechat.merchant.MerchantConfig;
import com.ajaxjs.wechat.merchant.SignerMaker;
import org.junit.jupiter.api.Test;

class TestSignerMaker {
    @Test
    void testRsa() {
        MerchantConfig cfg = new MerchantConfig();
        cfg.setMchId("1623777099");
        cfg.setMchSerialNo("6AF972CBD5E18A6A9EF2335A3D496FAA9BB5E74B");
        cfg.setPrivateKey("C:\\Users\\Z\\Downloads\\WXCertUtil\\cert\\1623777099_20251021_cert\\apiclient_key.pem");

        SignerMaker signerMaker = new SignerMaker(cfg);

        String s2 = new DoSignature(signerMaker.privateKey).signToBase64("message");
        System.out.println(s2);
    }
}
