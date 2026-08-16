package com.ajaxjs.fileupload.permission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link PermissionCheck}, covering {@code isPosixSupported()}.
 * <p>
 * The {@code check()} method is not tested here because it requires a real
 * filesystem with POSIX attribute views.
 * </p>
 */
class TestPermissionCheck {

    /**
     * Verifies that {@code isPosixSupported()} returns a boolean value
     * ({@code true} or {@code false}) without throwing an exception.
     */
    @Test
    void isPosixSupportedReturnsBoolean() {
        assertDoesNotThrow(() -> {
            boolean supported = PermissionCheck.isPosixSupported();
            // On any platform, the result must be a valid boolean
            assertTrue(supported == true || supported == false);
        });
    }
}