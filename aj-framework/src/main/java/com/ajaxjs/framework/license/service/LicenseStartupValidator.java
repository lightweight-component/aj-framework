package com.ajaxjs.framework.license.service;

import com.ajaxjs.framework.license.context.LicenseContext;
import com.ajaxjs.framework.license.entity.License;
import com.ajaxjs.framework.license.entity.LicenseVerifyResult;
import com.ajaxjs.util.io.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * 许可证启动验证器
 * 在应用启动时验证许可证并保存到上下文
 */
@Slf4j
//@Component
public class LicenseStartupValidator implements ApplicationRunner {
    @Autowired
    private KeyManagementService keyManagementService;

    @Value("${license.file.path:license.json}")
    private String licenseFilePath;

    @Value("${license.public.key.path:public.pem}")
    private String publicKeyPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始执行许可证启动验证...");

        String licenseJson = FileHelper.readFileContent(licenseFilePath);
        log.debug("许可证文件读取成功，文件大小: {} 字节", licenseJson.length());

        keyManagementService.loadPublicKey(FileHelper.readFileContent(publicKeyPath));// 加载公钥
        log.info("公钥文件加载成功");

        // 验证许可证
        LicenseVerifyResult result = LicenseService.verifyLicense(licenseJson, keyManagementService.getCachedPublicKey());

        if (!result.isValid()) {
            log.error("许可证验证失败: {}", result.getMessage());
            throw new RuntimeException("许可证验证失败，应用启动终止: " + result.getMessage());
        }

        License license = result.getLicense();
        log.info("====== 许可证验证通过 ======");
        log.info("软件产品: {}", license.getSubject());
        log.info("授权对象: {}", license.getIssuedTo());
        log.info("到期时间: {}", license.getExpireAt());
        log.info("授权功能: {}", String.join(", ", license.getFeatures()));
        log.info("绑定硬件: {}", license.getHardwareId());
        log.info("============================");

        LicenseContext.setCurrentLicense(license);// 将许可证信息保存到应用上下文，供其他组件使用
    }
}