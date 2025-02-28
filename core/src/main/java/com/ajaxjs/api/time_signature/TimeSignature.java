package com.ajaxjs.api.time_signature;

import com.ajaxjs.api.InterceptorAction;
import com.ajaxjs.util.cryptography.AesCrypto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Used for:
 * 1) App public API, not browser 2) make a link for password reset
 */
@EqualsAndHashCode(callSuper = true)
@Data
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
            timestampStr = AesCrypto.getInstance().AES_decode(signature, secretKey);
        } catch (Exception e) {
            throw new SecurityException("Invalid signature: " + signature);
        }

        long timestamp;

        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw new SecurityException("Invalid timestamp format.");
        }

        long now = System.currentTimeMillis();

        return Math.abs(now - timestamp) <= expirationTime;
    }

    /**
     * Generate the signature.
     *
     * @param timestamp The time stamp
     * @return signature
     */
    public String generateSignature(long timestamp) {
        String timestampStr = String.valueOf(timestamp);

        return AesCrypto.getInstance().AES_encode(timestampStr, secretKey);
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
