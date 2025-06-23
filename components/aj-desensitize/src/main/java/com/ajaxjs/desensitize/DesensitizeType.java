package com.ajaxjs.desensitize;

import java.util.function.Function;

/**
 * 脱敏类型
 */
public enum DesensitizeType {
    DEFAULT(v -> DataMask.PLACE_HOLDER),
    // 手机号
    PHONE(DataMask::maskPhoneNumber),
    // 银行卡号
    BANK_CARD(DataMask::maskBankCard),
    // 身份证号
    ID_CARD(DataMask::maskIdCard),
    // 姓名
    USERNAME(DataMask::maskChineseName),
    // email
    EMAIL(DataMask::maskEmail),
    //地址
    ADDRESS(v -> DataMask.maskAddress(v, 0));

    public final Function<String, String> handler;

    DesensitizeType(Function<String, String> handler) {
        this.handler = handler;
    }
}
