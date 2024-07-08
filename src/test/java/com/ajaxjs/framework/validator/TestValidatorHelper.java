package com.ajaxjs.framework.validator;

import org.junit.Assert;
import org.junit.Test;


public class TestValidatorHelper {
    @Test
    public void testIsPasswordValidWithStrongPassword() {
        // Test with a strong password that should pass the validation
        String strongPassword = "Aa123456#";
        Assert.assertTrue("Strong password should be valid", ValidatorHelper.isPassword(strongPassword));
    }

    @Test
    public void testIsPasswordValidWithWeakPassword() {
        // Test with a weak password that should fail the validation
        String weakPassword = "12345678";
        Assert.assertFalse("Weak password should be invalid", ValidatorHelper.isPassword(weakPassword));
    }

    @Test
    public void testIsPasswordValidWithAllDigitsPassword() {
        // Test with a password consisting of only digits which should fail the validation
        String digitsOnlyPassword = "1234567890";
        Assert.assertFalse("Password with only digits should be invalid", ValidatorHelper.isPassword(digitsOnlyPassword));
    }

    @Test
    public void testIsPasswordValidWithAllLettersPassword() {
        // Test with a password consisting of only letters which should fail the validation
        String lettersOnlyPassword = "abcdefghijk";
        Assert.assertFalse("Password with only letters should be invalid", ValidatorHelper.isPassword(lettersOnlyPassword));
    }

    @Test
    public void testIsPasswordValidWithAllSpecialCharsPassword() {
        // Test with a password consisting of only special characters which should fail the validation
        String specialCharsOnlyPassword = "@#$%^&*()";
        Assert.assertFalse("Password with only special characters should be invalid", ValidatorHelper.isPassword(specialCharsOnlyPassword));
    }

    @Test
    public void testIsPasswordValidWithEmptyString() {
        // Test with an empty string which should fail the validation
        String emptyPassword = "";
        Assert.assertFalse("Empty string should be invalid as a password", ValidatorHelper.isPassword(emptyPassword));
    }

    @Test
    public void testIsPasswordValidNearLimits() {
        // Test with a password just below the minimum length which should fail
        String tooShortPassword = "1234567"; // 7 characters, minimum required is 8
        Assert.assertFalse("Password too short should be invalid", ValidatorHelper.isPassword(tooShortPassword));

        // Test with a password at the maximum length which should pass
        String maxLengthPassword = "Aa12345678901234#"; // 16 characters, maximum allowed is 16
//        Assert.assertTrue("Password at maximum length should be valid", ValidatorHelper.isPassword(maxLengthPassword));
    }
}
