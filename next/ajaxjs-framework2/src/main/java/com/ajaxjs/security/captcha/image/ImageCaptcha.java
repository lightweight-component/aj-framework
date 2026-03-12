package com.ajaxjs.security.captcha.image;

import com.ajaxjs.security.InterceptorAction;
import com.ajaxjs.spring.Inter18n;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.RandomTools;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;

@Data
@Slf4j
@Component
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "security.ImageCaptcha.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "security.image-captcha")
public class ImageCaptcha extends InterceptorAction<ImageCaptchaCheck> {
    static String MESSAGE_KEY_PREFIX = "security.ImageCaptcha.message";

    @Override
    public boolean action(ImageCaptchaCheck annotation, HttpServletRequest req) {
        String uuid = req.getParameter("uuid");

        if (ObjectHelper.isEmptyText(uuid))
            throw new IllegalArgumentException(
                    i18n.getText(MESSAGE_KEY_PREFIX, "a3", "The parameter 'uuid' is required.")
            );

        String captchaCode = req.getParameter(CODE_KEY);

        if (ObjectHelper.isEmptyText(captchaCode))
            throw new IllegalArgumentException(
                    i18n.getText(MESSAGE_KEY_PREFIX, "a2", "The parameter '" + CODE_KEY + "' is required.", CODE_KEY)
            );

        String key = KEY_PREFIX + uuid;
        String captchaCodeInRam = config.getCaptchaCodeFromRam().apply(key);

        if (ObjectHelper.isEmptyText(captchaCodeInRam))
            throw new SecurityException(
                    i18n.getText(MESSAGE_KEY_PREFIX, "a4", "The captcha code doesn't exist or expired. Please try to generate it again.")
            );

        captchaCode = captchaCode.trim();

        if (!captchaCode.equalsIgnoreCase(captchaCodeInRam)) {
            log.info("user input:{}, ram:{}", captchaCode, captchaCodeInRam);

            throw new SecurityException(i18n.getText(MESSAGE_KEY_PREFIX, "a1", "The captcha code is incorrect."));
        }

        // 验证码验证成功后，删除验证码
        config.getRemoveByKey().accept(key);

        return true;
    }

    @Autowired
    Inter18n i18n;

    @Autowired
    ImageCaptchaConfig config;

    public final static String KEY_PREFIX = "captcha_";

    private final static String CODE_KEY = "captcha_code";

    /**
     * 验证码有效时间
     */
    int expireSeconds;

    public void captchaImage(HttpServletRequest req, HttpServletResponse resp) {

        String uuid = req.getParameter("uuid");

        if (ObjectHelper.isEmptyText(uuid) || "undefined".equals(uuid))
            throw new IllegalArgumentException("The parameter 'uuid' is required.");

        String capText = RandomTools.generateRandomString(4);// create the text for the image

        // store the text in the session
        log.info("store the text [{}] in the session [{}]", capText, uuid);
        config.getSaveToRam().save(KEY_PREFIX + uuid, capText, expireSeconds);

        resp.setDateHeader("Expires", 0);
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.addHeader("Cache-Control", "post-check=0, pre-check=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setContentType("image/jpeg");

        try {
            ImageIO.write(config.getCaptchaImageProvider().getRenderedImage(70, 30, capText), "jpg", resp.getOutputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
