package com.ajaxjs.security.captcha.image;

import com.ajaxjs.security.captcha.image.impl.SimpleCaptchaImage;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class TestSimpleCaptchaImage {
    @Test
    void test() throws IOException {
        boolean result = ImageIO.write(new SimpleCaptchaImage().getRenderedImage(200, 50, "343d"), "jpg",
                new File("/Users/xinzhang/code/code/temp/test.jpg"));

        if (!result)
            throw new Error("Unsupported file format");

        System.out.println("Image saved successfully ");
    }
}
