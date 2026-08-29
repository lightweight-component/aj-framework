package com.ajaxjs.security.captcha.image;

import java.awt.image.RenderedImage;

/**
 * A Provider for image of captcha
 */
public interface ICaptchaImageProvider {
    /**
     * Get rendered imageF
     *
     * @param width     the image width in pixels
     * @param height    the image height in pixels
     * @param randomStr the captcha text to render
     * @return the rendered captcha image
     */
    default RenderedImage getRenderedImage(int width, int height, String randomStr) {
        throw new NullPointerException();
    }
}
