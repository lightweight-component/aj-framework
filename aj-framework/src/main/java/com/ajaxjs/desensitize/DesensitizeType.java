package com.ajaxjs.desensitize;

/**
 * 脱敏类型
 */
public enum DesensitizeType {
    DEFAULT,
    // 手机号
    PHONE,
    // 银行卡号
    BANK_CARD,
    // 身份证号
    ID_CARD,
    // 姓名
    USERNAME,
    // email
    EMAIL,
    //地址
    ADDRESS;
}
