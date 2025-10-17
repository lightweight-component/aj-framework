package com.ajaxjs.framework.license.controller;

import com.ajaxjs.framework.license.context.HardwareUtil;
import com.ajaxjs.framework.license.entity.License;
import com.ajaxjs.framework.license.entity.LicenseVerifyResult;
import com.ajaxjs.framework.license.service.KeyManagementService;
import com.ajaxjs.framework.license.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public ResponseEntity<Map<String, Object>> generateKeys() throws Exception {
        Map<String, String> keys = keyManagementService.generateKeyPair();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", keys);
        return ResponseEntity.ok(response);
    }

    /**
     * 加载密钥
     */
    @PostMapping("/keys/load")
    public ResponseEntity<Map<String, Object>> loadKeys(@RequestBody Map<String, String> request) throws Exception {

        String privateKey = request.get("privateKey");
        String publicKey = request.get("publicKey");

        if (privateKey != null && !privateKey.trim().isEmpty())
            keyManagementService.loadPrivateKey(privateKey);

        if (publicKey != null && !publicKey.trim().isEmpty())
            keyManagementService.loadPublicKey(publicKey);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "密钥加载成功");
        return ResponseEntity.ok(response);

    }

    /**
     * 生成许可证
     */
    @PostMapping("/license/generate")
    public ResponseEntity<Map<String, Object>> generateLicense(@RequestBody License license) {
        if (!keyManagementService.isKeysLoaded()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "请先加载或生成密钥");
            return ResponseEntity.badRequest().body(response);
        }

        String licenseJson = LicenseService.generateLicense(license, keyManagementService.getCachedPrivateKey());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", licenseJson);

        return ResponseEntity.ok(response);
    }

    /**
     * 验证许可证
     */
    @PostMapping("/license/verify")
    public ResponseEntity<Map<String, Object>> verifyLicense(@RequestBody Map<String, String> request) {
        String licenseJson = request.get("licenseJson");

        if (!keyManagementService.isKeysLoaded()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "请先加载公钥");
            return ResponseEntity.badRequest().body(response);
        }

        LicenseVerifyResult result = LicenseService.verifyLicense(licenseJson, keyManagementService.getCachedPublicKey());

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isValid());
        response.put("message", result.getMessage());
        if (result.getLicense() != null)
            response.put("license", result.getLicense());

        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前硬件信息
     */
    @GetMapping("/hardware/info")
    public ResponseEntity<Map<String, Object>> getHardwareInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        Map<String, String> hardwareInfo = new HashMap<>();
        hardwareInfo.put("motherboardSerial", HardwareUtil.getMotherboardSerial());
        hardwareInfo.put("systemInfo", HardwareUtil.getSystemInfo());

        response.put("data", hardwareInfo);
        return ResponseEntity.ok(response);
    }

    /**
     * 检查密钥状态
     */
    @GetMapping("/keys/status")
    public ResponseEntity<Map<String, Object>> getKeysStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("keysLoaded", keyManagementService.isKeysLoaded());

        return ResponseEntity.ok(response);
    }
}