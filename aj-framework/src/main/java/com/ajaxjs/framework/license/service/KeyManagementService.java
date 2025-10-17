package com.ajaxjs.framework.license.service;


import com.ajaxjs.util.cryptography.RsaCrypto;
import com.ajaxjs.util.cryptography.rsa.KeyMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 密钥管理服务
 */
@Service
@Slf4j
public class KeyManagementService {
    // 内存中存储密钥（实际项目中应该从配置文件或密钥库加载）
    private PrivateKey cachedPrivateKey;

    private PublicKey cachedPublicKey;

    private static final int KEY_SIZE = 2048;

    /**
     * 生成RSA密钥对
     */
    public KeyPair _generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RsaCrypto.RSA);
        keyGen.initialize(KEY_SIZE);

        return keyGen.generateKeyPair();
    }

    /**
     * 从PEM格式字符串加载私钥
     */
    public PrivateKey loadPrivateKeyFromPem(String pemContent) throws Exception {
        String privateKeyPEM = pemContent
                .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance(RsaCrypto.RSA);

        return keyFactory.generatePrivate(spec);
    }

    /**
     * 从PEM格式字符串加载公钥
     */
    public PublicKey loadPublicKeyFromPem(String pemContent) throws Exception {
        String publicKeyPEM = pemContent
                .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance(RsaCrypto.RSA);

        return keyFactory.generatePublic(spec);
    }

    /**
     * 生成新的密钥对
     */
    public Map<String, String> generateKeyPair() throws Exception {
        KeyPair keyPair = _generateKeyPair();
        String privateKeyPem = KeyMgr.privateKeyToPem(keyPair.getPrivate());
        String publicKeyPem = KeyMgr.publicKeyToPem(keyPair.getPublic());

        // 缓存密钥
        this.cachedPrivateKey = keyPair.getPrivate();
        this.cachedPublicKey = keyPair.getPublic();

        Map<String, String> result = new HashMap<>();
        result.put("privateKey", privateKeyPem);
        result.put("publicKey", publicKeyPem);

        log.info("新密钥对生成成功");
        return result;
    }

    /**
     * 加载私钥
     */
    public void loadPrivateKey(String privateKeyPem) throws Exception {
        cachedPrivateKey = loadPrivateKeyFromPem(privateKeyPem);
        log.info("私钥加载成功");
    }

    /**
     * 加载公钥
     */
    public void loadPublicKey(String publicKeyPem) throws Exception {
        cachedPublicKey = loadPublicKeyFromPem(publicKeyPem);
        log.info("公钥加载成功");
    }

    /**
     * 获取缓存的私钥
     */
    public PrivateKey getCachedPrivateKey() {
        return cachedPrivateKey;
    }

    /**
     * 获取缓存的公钥
     */
    public PublicKey getCachedPublicKey() {
        return cachedPublicKey;
    }

    /**
     * 检查密钥是否已加载
     */
    public boolean isKeysLoaded() {
        return cachedPrivateKey != null && cachedPublicKey != null;
    }
}