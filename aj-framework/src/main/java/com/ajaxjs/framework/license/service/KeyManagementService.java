package com.ajaxjs.framework.license.service;


import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.cryptography.Constant;
import com.ajaxjs.util.cryptography.rsa.KeyMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
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
     * 生成新的密钥对
     */
    public Map<String, String> generateKeyPair() {
        KeyMgr keyMgr = new KeyMgr(Constant.RSA, KEY_SIZE);
        KeyPair keyPair = keyMgr.generateKeyPair();

        // 缓存密钥
        cachedPrivateKey = keyPair.getPrivate();
        cachedPublicKey = keyPair.getPublic();
        log.info("新密钥对生成成功");

        return ObjectHelper.mapOf("publicKey", keyMgr.getPublicToPem(), "privateKey", keyMgr.getPrivateToPem());
    }

    /**
     * 加载私钥
     */
    public void loadPrivateKey(String privateKeyPem) {
        cachedPrivateKey = (PrivateKey) KeyMgr.restoreKey(false, privateKeyPem);
    }

    /**
     * 加载公钥
     */
    public void loadPublicKey(String publicKeyPem) {
        cachedPublicKey = (PublicKey) KeyMgr.restoreKey(true, publicKeyPem);
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