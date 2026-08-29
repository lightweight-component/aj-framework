package com.ajaxjs.security.desensitize;

import lombok.AllArgsConstructor;

import java.util.function.Function;

/**
 * 脱敏类型
 */
@AllArgsConstructor
public enum DesensitizeType {
    /**
     * Replaces the value with the default placeholder.
     */
    DEFAULT(v -> DataMask.PLACE_HOLDER),
    /**
     * Masks a phone number.
     */
    PHONE(DataMask::maskPhoneNumber),
    /**
     * Masks a bank-card number.
     */
    BANK_CARD(DataMask::maskBankCard),
    /**
     * Masks an identity-card number.
     */
    ID_CARD(DataMask::maskIdCard),
    /**
     * Masks a Chinese name.
     */
    USERNAME(DataMask::maskChineseName),
    /**
     * Masks an email address.
     */
    EMAIL(DataMask::maskEmail),
    /**
     * Masks an address.
     */
    ADDRESS(v -> DataMask.maskAddress(v, 0));

    /**
     * Stores the handler value.
     */
    public final Function<String, String> handler;
}
