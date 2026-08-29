package com.ajaxjs.security.timesignature;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Represents the test time sign component.
 */
class TestTimeSign {
    /**
     * Stores the secret key value.
     */
    private static final String SECRET_KEY = "der3@x7Az#2";

    /**
     * Stores the now value.
     */
    private static final long NOW = 1_700_000_000_000L;

    /**
     * Stores the time signature value.
     */
    private final TimeSignature timeSignature = createTimeSignature();

    /**
     * Creates a time-signature verifier with a deterministic clock.
     */
    private static TimeSignature createTimeSignature() {
        TimeSignature timeSignature = new TimeSignature();
        timeSignature.setSecretKey(SECRET_KEY);
        timeSignature.setClock(Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC));

        return timeSignature;
    }

    @Test
    void testGenerateSignature() {
        String signature = timeSignature.generateSignature();
        assertNotNull(signature);

        assertTrue(timeSignature.verifySignature(signature));
    }

    @Test
    void testErrorSignature() {
        String signature = "A785A0ADA9949DAF6C410202CF1E0A1C";
        SecurityException exception = assertThrows(SecurityException.class, () -> timeSignature.verifySignature(signature));
        assertFalse(exception.getMessage().contains(signature));
    }

    /**
     * Verifies that Lombok-generated text does not expose the secret key.
     */
    @Test
    void testToStringDoesNotExposeSecretKey() {
        assertFalse(timeSignature.toString().contains(SECRET_KEY));
    }

    @Test
    void testGenerateSignatureOvertime() {
        String signature = timeSignature.generateSignature(NOW - 30 * 60_000L);
        assertFalse(timeSignature.verifySignature(signature));
    }

    /**
     * Verifies that malformed inputs are reported without exposing their contents.
     */
    @Test
    void testInvalidInput() {
        assertThrows(SecurityException.class, () -> timeSignature.verifySignature(null));
        assertThrows(SecurityException.class, () -> timeSignature.verifySignature(" "));
    }

    /**
     * Verifies that future timestamps are limited to the configured clock skew.
     */
    @Test
    void testFutureTimestamp() {
        timeSignature.setAllowedClockSkewSeconds(30);

        assertTrue(timeSignature.verifySignature(timeSignature.generateSignature(NOW + 30_000L)));
        assertFalse(timeSignature.verifySignature(timeSignature.generateSignature(NOW + 30_001L)));
    }

    /**
     * Verifies the exact expiration boundary.
     */
    @Test
    void testExpirationBoundary() {
        timeSignature.setExpirationMin(1);

        assertTrue(timeSignature.verifySignature(timeSignature.generateSignature(NOW - 59_999L)));
        assertFalse(timeSignature.verifySignature(timeSignature.generateSignature(NOW - 60_000L)));
    }

    /**
     * Verifies that changing expiration configuration immediately affects validation.
     */
    @Test
    void testExpirationConfiguration() {
        String signature = timeSignature.generateSignature(NOW - 20 * 60_000L);

        assertFalse(timeSignature.verifySignature(signature));
        timeSignature.setExpirationMin(30);
        assertTrue(timeSignature.verifySignature(signature));
    }

    /**
     * Verifies that extreme timestamp values cannot bypass validation through overflow.
     */
    @Test
    void testTimestampOverflow() {
        assertFalse(timeSignature.verifySignature(timeSignature.generateSignature(Long.MIN_VALUE)));
        assertFalse(timeSignature.verifySignature(timeSignature.generateSignature(Long.MAX_VALUE)));
    }

    /**
     * Verifies that an MVC request containing a valid tsign parameter is accepted.
     */
    @Test
    void testActionWithValidRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("tsign", timeSignature.generateSignature());

        assertTrue(timeSignature.action(null, request));
    }

    /**
     * Verifies that an MVC request without the required tsign parameter is rejected.
     */
    @Test
    void testActionWithMissingSignature() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> timeSignature.action(null, request));
        assertEquals("Missing Parameters: tsign.", exception.getMessage());
    }

    /**
     * Verifies that an invalid request signature is rejected without exposing the credential.
     */
    @Test
    void testActionWithInvalidSignature() {
        String signature = "invalid-signature";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("tsign", signature);

        SecurityException exception = assertThrows(SecurityException.class,
                () -> timeSignature.action(null, request));
        assertFalse(exception.getMessage().contains(signature));
    }

    @Test
    void testSaturatedTimestampArithmetic() {
        assertEquals(Long.MAX_VALUE, TimeSignature.saturatedAdd(Long.MAX_VALUE, 1));
        assertEquals(Long.MIN_VALUE, TimeSignature.saturatedSubtract(Long.MIN_VALUE, 1));
        assertEquals(3L, TimeSignature.saturatedAdd(1, 2));
        assertEquals(-1L, TimeSignature.saturatedSubtract(1, 2));
    }

}
