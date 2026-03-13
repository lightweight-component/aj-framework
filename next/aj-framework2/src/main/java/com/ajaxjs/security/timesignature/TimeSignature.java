package com.ajaxjs.security.timesignature;

import com.ajaxjs.security.InterceptorAction;
import com.ajaxjs.util.cryptography.Cryptography;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Used for:
 * 1) App public API, not browser 2) make a link for password reset
 */
@Data
@Component
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "security.TimeSignature.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "security.time-signature")
public class TimeSignature extends InterceptorAction<TimeSignatureVerify> {
    /**
     * 秘钥，需要保密
     */
    private String secretKey = "your_secret_key";

    /**
     * 默认 15分钟的过期时间
     */
    private int expirationMin = 15;

    private long expirationTime = expirationMin * 60 * 1000;

    /**
     * Verify the time signature, check the time if is overtime.
     *
     * @param signature The signature
     * @return Whether the signature is valid
     */
    public boolean verifySignature(String signature) {
        String timestampStr;

        try {
            timestampStr = Cryptography.AES_decode(signature, secretKey);
        } catch (Exception e) {
            throw new SecurityException("Invalid signature: " + signature);
        }

        long timestamp;

        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw new SecurityException("Invalid timestamp format.");
        }

        return Math.abs(System.currentTimeMillis() - timestamp) < expirationTime;
    }

    /**
     * Generate the signature.
     *
     * @param timestamp The time stamp
     * @return signature
     */
    public String generateSignature(long timestamp) {
        String timestampStr = String.valueOf(timestamp);

        return Cryptography.AES_encode(timestampStr, secretKey);
    }

    /**
     * Generate the signature.
     *
     * @return signature
     */
    public String generateSignature() {
        return generateSignature(System.currentTimeMillis());
    }

    @Override
    public boolean action(TimeSignatureVerify annotation, HttpServletRequest req) {
        String signature = req.getParameter("tsign");// 获取签名参数

        if (!StringUtils.hasText(signature))
            throw new IllegalArgumentException("Missing Parameters: tsign.");

        if (!verifySignature(signature))
            throw new SecurityException("Invalid or expired signature.");

        return true;
    }
}
