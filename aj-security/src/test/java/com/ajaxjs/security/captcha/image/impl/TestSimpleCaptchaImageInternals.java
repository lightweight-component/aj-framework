package com.ajaxjs.security.captcha.image.impl;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests package-visible image-captcha helper methods.
 */
class TestSimpleCaptchaImageInternals {
    @Test
    void testRandomColorWithinRequestedBounds() {
        Color color = SimpleCaptchaImage.getRandColor(100, 120);

        assertTrue(color.getRed() >= 100 && color.getRed() < 120);
        assertTrue(color.getGreen() >= 100 && color.getGreen() < 120);
        assertTrue(color.getBlue() >= 100 && color.getBlue() < 120);
    }
}
