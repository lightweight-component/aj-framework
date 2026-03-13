package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.cryptography.rsa.DoSignature;
import com.ajaxjs.util.cryptography.rsa.DoVerify;
import com.ajaxjs.util.cryptography.rsa.KeyMgr;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCryptography {
    final String key = "abc";
    final String word = "123";

    @Test
    void testAES() {
        String encWord = Cryptography.AES_encode(word, key);
        assertEquals(word, Cryptography.AES_decode(encWord, key));
    }

    @Test
    void testDES() {
        String encWord = Cryptography.DES_encode(word, key);
        assertEquals(word, Cryptography.DES_decode(encWord, key));
    }

    @SuppressWarnings("restriction")
    @Test
    void test3DES() {
        // 添加新安全算法,如果用 JCE 就要把它添加进去
        // 这里 addProvider 方法是增加一个新的加密算法提供者(个人理解没有找到好的答案,求补充)
//		Security.addProvider(new com.sun.crypto.provider.SunJCE());
        // byte 数组(用来生成密钥的)
        final byte[] keyBytes = {0x11, 0x22, 0x4F, 0x58, (byte) 0x88, 0x10, 0x40, 0x38, 0x28, 0x25, 0x79, 0x51, (byte) 0xCB, (byte) 0xDD, 0x55, 0x66, 0x77, 0x29, 0x74,
                (byte) 0x98, 0x30, 0x40, 0x36, (byte) 0xE2};
        String word = "This is a 3DES test. 测试";

        byte[] encoded = Cryptography.tripleDES_encode(word, keyBytes);

        assertEquals(word, Cryptography.tripleDES_decode(encoded, keyBytes));
    }

    @Test
    void testPBE() {
        byte[] salt = Cryptography.initSalt();
        byte[] encData = Cryptography.PBE_encode(word, key, salt, 100);

        assertEquals(word, Cryptography.PBE_decode(encData, key, salt, 100));
    }

    @Test
    void testDoSignature() {
// 生成公钥私钥
        KeyMgr keyMgr = new KeyMgr(Constant.RSA, 1024);
        keyMgr.generateKeyPair();
        String privateKey = keyMgr.getPrivateKeyStr();

        byte[] helloWorlds = new DoSignature(Constant.SHA256_RSA).setStrData("hello world").setPrivateKeyStr(privateKey).sign();
        String result = new DoSignature(Constant.SHA256_RSA).setStrData("hello world").setPrivateKeyStr(privateKey).signToString();

        assertEquals(new Base64Utils(helloWorlds).encodeAsString(), result);
    }

    @Test
    void testDoVerify() {
// 生成公钥私钥
        KeyMgr keyMgr = new KeyMgr(Constant.RSA, 1024);
        keyMgr.generateKeyPair();
        String publicKey = keyMgr.getPublicKeyStr(), privateKey = keyMgr.getPrivateKeyStr();
        String result = new DoSignature(Constant.SHA256_RSA).setStrData("hello world").setPrivateKeyStr(privateKey).signToString();
        boolean verified = new DoVerify(Constant.SHA256_RSA).setStrData("hello world").setPublicKeyStr(publicKey).setSignatureBase64(result).verify();

        assertTrue(verified);
    }

    @Test
    public void testRSA() {
        // 生成公钥私钥
        KeyMgr keyMgr = new KeyMgr(Constant.RSA, 1024);
        keyMgr.generateKeyPair();
        String publicKey = keyMgr.getPublicKeyStr(), privateKey = keyMgr.getPrivateKeyStr();

        System.out.println("公钥: \n\r" + publicKey);
        System.out.println("私钥： \n\r" + privateKey);
//		System.out.println("公钥加密--------私钥解密");

        String word = "你好，世界！";

        byte[] encWord = KeyMgr.publicKeyEncrypt(word.getBytes(), publicKey);
        String decWord = new String(KeyMgr.privateKeyDecrypt(encWord, privateKey));

        String eBody = new Base64Utils(encWord).encodeAsString();
        String decWord2 = new String(KeyMgr.privateKeyDecrypt(new Base64Utils(eBody).decode(), privateKey));
        System.out.println("加密前: " + word + "\n\r密文：" + eBody + "\n解密后: " + decWord2);
        assertEquals(word, decWord);

//		System.out.println("私钥加密--------公钥解密");

        String english = "Hello, World!";
        byte[] encEnglish = KeyMgr.privateKeyEncrypt(english.getBytes(), privateKey);
        String decEnglish = new String(KeyMgr.publicKeyDecrypt(encEnglish, publicKey));
//		System.out.println("加密前: " + english + "\n\r" + "解密后: " + decEnglish);

        assertEquals(english, decEnglish);
//		System.out.println("私钥签名——公钥验证签名");

// 产生签名
        String sign = new DoSignature(Constant.MD5_RSA).setPrivateKeyStr(privateKey).setData(encEnglish).signToString();
//		System.out.println("签名:\r" + sign);
// 验证签名
        assertTrue(new DoVerify(Constant.MD5_RSA).setPublicKeyStr(publicKey).setData(encEnglish).setSignatureBase64(sign).verify());
    }
}