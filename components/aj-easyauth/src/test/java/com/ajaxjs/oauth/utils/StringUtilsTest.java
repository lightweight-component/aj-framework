package com.ajaxjs.oauth.utils;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {

    @Test
    public void isEmptyNonEmptyInput() {
        Assertions.assertFalse(StringUtils.isEmpty("non-empty string"));
    }

    @Test
    public void isEmptyEmptyInput() {
        Assertions.assertTrue(StringUtils.isEmpty(""));
    }

    @Test
    public void isEmptyInputNull() {
        Assertions.assertTrue(StringUtils.isEmpty(null));
    }

    @Test
    public void isNotEmptyNonEmptyInput() {
        Assertions.assertTrue(StringUtils.isNotEmpty("non-empty string"));
    }

    @Test
    public void isNotEmptyEmptyInput() {
        Assertions.assertFalse(StringUtils.isNotEmpty(""));
    }

    @Test
    public void isNotEmptyInputNull() {
        Assertions.assertFalse(StringUtils.isNotEmpty(null));
    }

    @Test
    public void appendIfNotContainAppendedStringNotPresent() {
        // (Check the case where appendStr doesn't occur in str)
        final String str = "Prefix ";
        final String appendStr = "suffix";
        final String otherwise = "should be discarded";

        final String result =
            StringUtils.appendIfNotContain(str, appendStr, otherwise);

        Assertions.assertEquals("Prefix suffix", result);
    }

    @Test
    public void appendIfNotContainAppendedStringPresent() {
        // (Check the case where appendStr occurs in str)
        final String str = "Prefix ";
        final String appendStr = "Prefix";
        final String otherwise = "should be appended";

        final String result =
            StringUtils.appendIfNotContain(str, appendStr, otherwise);

        Assertions.assertEquals("Prefix should be appended", result);
    }

    @Test
    public void appendIfNotContainEmptyString() {
        // (Check the special-case for str being empty)
        final String str = "";
        final String appendStr = "should not be appended";
        final String otherwise = "should also not be appended";

        final String result =
            StringUtils.appendIfNotContain(str, appendStr, otherwise);

        Assertions.assertEquals("", result);
    }

    @Test
    public void appendIfNotContainAppendingEmptyString() {
        // (Check the special-case for appendStr being empty)
        final String str = "should be kept";
        final String appendStr = "";
        final String otherwise = "should also not be appended";

        final String result =
            StringUtils.appendIfNotContain(str, appendStr, otherwise);

        Assertions.assertEquals("should be kept", result);
    }
}
