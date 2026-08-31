package com.ajaxjs.framework.validator;

import com.ajaxjs.framework.validator.annotation.*;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DirectFieldBindingResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestValidatorHelper {
    @Test
    void validatesBuiltInCommonConstraints() {
        DirectFieldBindingResult invalidErrors = new DirectFieldBindingResult(new InvalidStandardBean(), "bean");
        new ValidatorImpl().validate(invalidErrors.getTarget(), invalidErrors);
        assertTrue(invalidErrors.hasFieldErrors("notNull"));
        assertTrue(invalidErrors.hasFieldErrors("notBlank"));
        assertTrue(invalidErrors.hasFieldErrors("size"));
        assertTrue(invalidErrors.hasFieldErrors("min"));
        assertTrue(invalidErrors.hasFieldErrors("max"));
        assertTrue(invalidErrors.hasFieldErrors("pattern"));
        assertTrue(invalidErrors.hasFieldErrors("email"));

        DirectFieldBindingResult validErrors = new DirectFieldBindingResult(new ValidStandardBean(), "bean");
        new ValidatorImpl().validate(validErrors.getTarget(), validErrors);
        assertFalse(validErrors.hasErrors());
    }

    @Test
    void customIdCardIsStillValidated() {
        DirectFieldBindingResult errors = new DirectFieldBindingResult(new IdCardBean(), "bean");

        new ValidatorImpl().validate(errors.getTarget(), errors);

        assertTrue(errors.hasFieldErrors("idCard"));
    }

    private static class IdCardBean {
        @IdCard
        private String idCard = "invalid";
    }

    private static class InvalidStandardBean {
        @NotNull
        private Integer notNull;

        @NotBlank
        private String notBlank = " ";

        @Size(min = 3, max = 4)
        private String size = "ab";

        @Min(10)
        private Long min = 9L;

        @Max(20)
        private Integer max = 21;

        @Pattern(regexp = "[A-Z]+")
        private String pattern = "lowercase";

        @Email
        private String email = "invalid";
    }

    private static class ValidStandardBean {
        @NotNull
        private Integer notNull = 1;

        @NotBlank
        private String notBlank = "text";

        @Size(min = 3, max = 4)
        private String size = "abc";

        @Min(10)
        private Long min = 10L;

        @Max(20)
        private Integer max = 20;

        @Pattern(regexp = "[A-Z]+")
        private String pattern = "TEXT";

        @Pattern(regexp = "[A-Z]+", flags = Pattern.Flag.CASE_INSENSITIVE)
        private String patternWithFlag = "lowercase";

        @Email
        private String email = "test@example.com";

        @Size(min = 1)
        private String optionalSize;

        @Min(1)
        private Integer optionalMin;

        @Email
        private String optionalEmail;

        @Email
        private String emptyEmail = "";
    }

    @Test
    void validatesUsernameAndMobile() {
        assertTrue(ValidatorHelper.isUsername("tester_1"));
        assertFalse(ValidatorHelper.isUsername("1tester"));
        assertTrue(ValidatorHelper.isMobile("13812345678"));
        assertFalse(ValidatorHelper.isMobile("12345678901"));
        assertFalse(ValidatorHelper.isMobile(null));
    }

    @Test
    void validatesEmailAndChinese() {
        assertTrue(ValidatorHelper.isEmail("test@example.com"));
        assertFalse(ValidatorHelper.isEmail("not-an-email"));
        assertFalse(ValidatorHelper.isEmail("test|name@example.com"));
        assertTrue(ValidatorHelper.isChinese("中文"));
        assertFalse(ValidatorHelper.isChinese("A"));
    }

    @Test
    void validatesIdCardUrlAndIpAddress() {
        assertTrue(ValidatorHelper.isIDCard("11010519491231002X"));
        assertFalse(ValidatorHelper.isIDCard("110105194912310021"));
        assertFalse(ValidatorHelper.isIDCard("11010519491331002X"));
        assertFalse(ValidatorHelper.isIDCard("110105991332123"));
        assertTrue(ValidatorHelper.isUrl("https://example.com/path?a=1"));
        assertFalse(ValidatorHelper.isUrl("example.com"));
        assertFalse(ValidatorHelper.isUrl("https://example.com/path with space"));
        assertTrue(ValidatorHelper.isIpAddress("192.168.1.1"));
        assertFalse(ValidatorHelper.isIpAddress("192.168.1.256"));
        assertFalse(ValidatorHelper.isIpAddress("255"));
    }

    @Test
    void validatesDatesAndHandlesNullValues() {
        assertTrue(ValidatorHelper.isValidDate("20240229"));
        assertFalse(ValidatorHelper.isValidDate("20230229"));
        assertFalse(ValidatorHelper.isUsername(null));
        assertFalse(ValidatorHelper.isPassword(null));
        assertFalse(ValidatorHelper.isEmail(null));
        assertFalse(ValidatorHelper.isChinese(null));
        assertFalse(ValidatorHelper.isUrl(null));
        assertFalse(ValidatorHelper.isIpAddress(null));
    }

    @Test
    void testIsPasswordValidWithStrongPassword() {
        // Test with a strong password that should pass the validation
        String strongPassword = "Aa123456#";
        assertTrue(ValidatorHelper.isPassword(strongPassword));
    }

    @Test
    void testIsPasswordValidWithWeakPassword() {
        // Test with a weak password that should fail the validation
        String weakPassword = "12345678";
        assertFalse(ValidatorHelper.isPassword(weakPassword));
    }

    @Test
    void testIsPasswordValidWithAllDigitsPassword() {
        // Test with a password consisting of only digits which should fail the validation
        String digitsOnlyPassword = "1234567890";
        assertFalse(ValidatorHelper.isPassword(digitsOnlyPassword));
    }

    @Test
    void testIsPasswordValidWithAllLettersPassword() {
        // Test with a password consisting of only letters which should fail the validation
        String lettersOnlyPassword = "abcdefghijk";
        assertFalse(ValidatorHelper.isPassword(lettersOnlyPassword));
    }

    @Test
    void testIsPasswordValidWithAllSpecialCharsPassword() {
        // Test with a password consisting of only special characters which should fail the validation
        String specialCharsOnlyPassword = "@#$%^&*()";
        assertFalse(ValidatorHelper.isPassword(specialCharsOnlyPassword));
    }

    @Test
    void testIsPasswordValidWithEmptyString() {
        // Test with an empty string which should fail the validation
        String emptyPassword = "";
        assertFalse(ValidatorHelper.isPassword(emptyPassword));
    }

    @Test
    void testIsPasswordValidNearLimits() {
        // Test with a password just below the minimum length which should fail
        String tooShortPassword = "1234567"; // 7 characters, minimum required is 8
        assertFalse(ValidatorHelper.isPassword(tooShortPassword));

        // Test with a password at the maximum length which should pass
        String maxLengthPassword = "Aa12345678901234#"; // 16 characters, maximum allowed is 16
//        Assert.assertTrue("Password at maximum length should be valid", ValidatorHelper.isPassword(maxLengthPassword));
    }
}
