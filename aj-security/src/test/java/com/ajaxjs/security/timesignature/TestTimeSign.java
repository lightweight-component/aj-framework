package com.ajaxjs.security.timesignature;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class TestTimeSign {
    private static final String SECRET_KEY = "der3@x7Az#2";
    private static final long NOW = 1_700_000_000_000L;

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
    public void testGenerateSignature() {
        String signature = timeSignature.generateSignature();
        assertNotNull(signature);

        assertTrue(timeSignature.verifySignature(signature));
    }

    @Test
    public void testErrorSignature() {
        String signature = "A785A0ADA9949DAF6C410202CF1E0A1C";
        SecurityException exception = assertThrows(SecurityException.class, () -> timeSignature.verifySignature(signature));
        assertFalse(exception.getMessage().contains(signature));
    }

    /**
     * Verifies that Lombok-generated text does not expose the secret key.
     */
    @Test
    public void testToStringDoesNotExposeSecretKey() {
        assertFalse(timeSignature.toString().contains(SECRET_KEY));
    }

    @Test
    public void testGenerateSignatureOvertime() {
        String signature = timeSignature.generateSignature(NOW - 30 * 60_000L);
        assertFalse(timeSignature.verifySignature(signature));
    }

    /**
     * Verifies that malformed inputs are reported without exposing their contents.
     */
    @Test
    public void testInvalidInput() {
        assertThrows(SecurityException.class, () -> timeSignature.verifySignature(null));
        assertThrows(SecurityException.class, () -> timeSignature.verifySignature(" "));
    }

    /**
     * Verifies that future timestamps are limited to the configured clock skew.
     */
    @Test
    public void testFutureTimestamp() {
        timeSignature.setAllowedClockSkewSeconds(30);

        assertTrue(timeSignature.verifySignature(timeSignature.generateSignature(NOW + 30_000L)));
        assertFalse(timeSignature.verifySignature(timeSignature.generateSignature(NOW + 30_001L)));
    }

    /**
     * Verifies the exact expiration boundary.
     */
    @Test
    public void testExpirationBoundary() {
        timeSignature.setExpirationMin(1);

        assertTrue(timeSignature.verifySignature(timeSignature.generateSignature(NOW - 59_999L)));
        assertFalse(timeSignature.verifySignature(timeSignature.generateSignature(NOW - 60_000L)));
    }

    /**
     * Verifies that changing expiration configuration immediately affects validation.
     */
    @Test
    public void testExpirationConfiguration() {
        String signature = timeSignature.generateSignature(NOW - 20 * 60_000L);

        assertFalse(timeSignature.verifySignature(signature));
        timeSignature.setExpirationMin(30);
        assertTrue(timeSignature.verifySignature(signature));
    }

    /**
     * Verifies that extreme timestamp values cannot bypass validation through overflow.
     */
    @Test
    public void testTimestampOverflow() {
        assertFalse(timeSignature.verifySignature(timeSignature.generateSignature(Long.MIN_VALUE)));
        assertFalse(timeSignature.verifySignature(timeSignature.generateSignature(Long.MAX_VALUE)));
    }

    /**
     * Verifies that an MVC request containing a valid tsign parameter is accepted.
     */
    @Test
    public void testActionWithValidRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("tsign", timeSignature.generateSignature());

        assertTrue(timeSignature.action(null, request));
    }

    /**
     * Verifies that an MVC request without the required tsign parameter is rejected.
     */
    @Test
    public void testActionWithMissingSignature() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> timeSignature.action(null, request));
        assertEquals("Missing Parameters: tsign.", exception.getMessage());
    }

    /**
     * Verifies that an invalid request signature is rejected without exposing the credential.
     */
    @Test
    public void testActionWithInvalidSignature() {
        String signature = "invalid-signature";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("tsign", signature);

        SecurityException exception = assertThrows(SecurityException.class,
                () -> timeSignature.action(null, request));
        assertFalse(exception.getMessage().contains(signature));
    }

}
