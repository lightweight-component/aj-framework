package com.ajaxjs.security.captcha.image;

import lombok.Data;

import java.util.function.Consumer;
import java.util.function.Function;

@Data
public class ImageCaptchaConfig {
    ICaptchaImageProvider captchaImageProvider;

    SaveToRam saveToRam;

    Function<String, String> captchaCodeFromRam;

    Consumer<String> removeByKey;
}
