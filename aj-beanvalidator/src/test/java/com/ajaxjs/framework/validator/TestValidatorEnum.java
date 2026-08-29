package com.ajaxjs.framework.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestValidatorEnum {
    @Test
    void getsValidatorByAnnotationName() {
        assertEquals(ValidatorEnum.IdCard, ValidatorEnum.getInstance("IdCard"));
        assertNull(ValidatorEnum.getInstance("Unknown"));
    }

    @Test
    void validatesSupportedValues() {
        assertDoesNotThrow(() -> ValidatorEnum.IdCard.validated("11010519491231002X", "error"));
        assertDoesNotThrow(() -> ValidatorEnum.UserMail.validated("test@example.com", "error"));
        assertDoesNotThrow(() -> ValidatorEnum.NotBlank.validated("text", "error"));
        assertDoesNotThrow(() -> ValidatorEnum.NotNull.validated(1, "error"));
        assertDoesNotThrow(() -> ValidatorEnum.MobileNo.validated("13812345678", "error"));
        assertDoesNotThrow(() -> ValidatorEnum.Username.validated("tester_1", "error"));
        assertDoesNotThrow(() -> ValidatorEnum.Password.validated("Aa123456#", "error"));
        assertDoesNotThrow(() -> ValidatorEnum.Chinese.validated("中文", "error"));
        assertDoesNotThrow(() -> ValidatorEnum.Ipv4.validated("192.168.1.1", "error"));
        assertDoesNotThrow(() -> ValidatorEnum.HttpUrl.validated("https://example.com", "error"));
    }

    @Test
    void rejectsUnsupportedValues() {
        assertThrows(ValidatorException.class, () -> ValidatorEnum.IdCard.validated("invalid", "error"));
        assertThrows(ValidatorException.class, () -> ValidatorEnum.UserMail.validated("invalid", "error"));
        assertThrows(ValidatorException.class, () -> ValidatorEnum.NotBlank.validated(" ", "error"));
        assertThrows(ValidatorException.class, () -> ValidatorEnum.NotNull.validated(null, "error"));
        assertThrows(ValidatorException.class, () -> ValidatorEnum.MobileNo.validated("123", "error"));
        assertThrows(ValidatorException.class, () -> ValidatorEnum.Username.validated("name", "error"));
        assertThrows(ValidatorException.class, () -> ValidatorEnum.Password.validated("12345678", "error"));
        assertThrows(ValidatorException.class, () -> ValidatorEnum.Chinese.validated("Chinese", "error"));
        assertThrows(ValidatorException.class, () -> ValidatorEnum.Ipv4.validated("192.168.1.256", "error"));
        assertThrows(ValidatorException.class, () -> ValidatorEnum.HttpUrl.validated("example.com", "error"));
    }
}
