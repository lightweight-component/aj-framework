package com.ajaxjs.framework.license.controller;

import com.ajaxjs.framework.license.context.HardwareUtil;
import com.ajaxjs.framework.license.entity.License;
import com.ajaxjs.framework.license.entity.LicenseVerifyResult;
import com.ajaxjs.framework.license.service.KeyManagementService;
import com.ajaxjs.framework.license.service.LicenseService;
import com.ajaxjs.util.ObjectHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 许可证控制器
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LicenseController {
    @Autowired
    private KeyManagementService keyManagementService;

    /**
     * 生成新的密钥对
     */
    @PostMapping("/keys/generate")
    public Map<String, String> generateKeys() {
        return keyManagementService.generateKeyPair();
    }

    /**
     * 加载密钥
     */
    @PostMapping("/keys/load")
    public boolean loadKeys(@RequestBody Map<String, String> request) {
        String privateKey = request.get("privateKey");
        String publicKey = request.get("publicKey");

        if (privateKey != null && !privateKey.trim().isEmpty())
            keyManagementService.loadPrivateKey(privateKey);

        if (publicKey != null && !publicKey.trim().isEmpty())
            keyManagementService.loadPublicKey(publicKey);

        return true;
    }

    /**
     * 生成许可证
     */
    @PostMapping("/license/generate")
    public Map<String, Object> generateLicense(@RequestBody License license) {
        if (!keyManagementService.isKeysLoaded())
            throw new UnsupportedOperationException("请先加载或生成密钥");

        String licenseJson = LicenseService.generateLicense(license, keyManagementService.getCachedPrivateKey());

        return ObjectHelper.mapOf("data", licenseJson);
    }

    /**
     * 验证许可证
     */
    @PostMapping("/license/verify")
    public LicenseVerifyResult verifyLicense(@RequestBody Map<String, String> request) {
        String licenseJson = request.get("licenseJson");

        if (!keyManagementService.isKeysLoaded())
            throw new UnsupportedOperationException("请先加载公钥");

        return LicenseService.verifyLicense(licenseJson, keyManagementService.getCachedPublicKey());
    }

    /**
     * 获取当前硬件信息
     */
    @GetMapping("/hardware/info")
    public Map<String, String> getHardwareInfo() {
        return ObjectHelper.mapOf("motherboardSerial", HardwareUtil.getMotherboardSerial(), "systemInfo", HardwareUtil.getSystemInfo());
    }

    /**
     * 检查密钥状态
     */
    @GetMapping("/keys/status")
    public boolean getKeysStatus() {
        return keyManagementService.isKeysLoaded();
    }
}