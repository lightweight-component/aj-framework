package com.ajaxjs.framework.license.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 验证结果类
 */
@Data
@AllArgsConstructor
public class LicenseVerifyResult {
    private final boolean valid;

    private final String message;

    private final License license;

    public LicenseVerifyResult(boolean valid, String message) {
        this(valid, message, null);
    }
}
