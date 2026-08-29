package com.ajaxjs.security.classic;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests package-visible classic security utility methods.
 */
class TestClassicInternals {
    @Test
    void testCleanAndListMembership() {
        assertEquals("abc", InstallFilter.clean("a1b2c3", Pattern.compile("\\d"), matcher -> ""));
        assertTrue(InstallFilter.isInList("admin", Arrays.asList("admin", "root")));
        assertFalse(InstallFilter.isInList("guest", Arrays.asList("admin")));
        assertTrue(ListCheck.isInList("admin", Arrays.asList("admin")));
        assertFalse(ListCheck.isInList("guest", Arrays.asList("admin")));
    }
}
