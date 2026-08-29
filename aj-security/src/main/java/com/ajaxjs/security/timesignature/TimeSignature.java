package com.ajaxjs.security.timesignature;

import com.ajaxjs.security.InterceptorAction;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.cryptography.Cryptography;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Clock;

/**
 * Used for:
 * 1) App public API, not browser 2) make a link for password reset
 */
@EqualsAndHashCode(callSuper = true)
@Component
@Data
@ConditionalOnProperty(name = "security.time-signature.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "security.time-signature")
public class TimeSignature extends InterceptorAction<TimeSignatureVerify> {
    /**
     * 秘钥，需要保密
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String secretKey;

    /**
     * 默认 15分钟的过期时间
     */
    private int expirationMin = 15;

    /**
     * Maximum number of seconds that a timestamp may be ahead of the server clock.
     */
    private int allowedClockSkewSeconds = 60;

    /**
     * Clock used for time validation.
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Clock clock = Clock.systemUTC();

    /**
     * Replaces the clock for deterministic package-level tests.
     *
     * @param clock the clock to use
     */
    void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Verify the time signature, check the time if is overtime.
     *
     * @param signature The signature
     * @return Whether the signature is valid
     */
    public boolean verifySignature(String signature) {
        String timestampStr;

        if (ObjectHelper.isEmptyText(secretKey))
            throw new IllegalArgumentException("The secretKey is not set.");

        try {
            timestampStr = Cryptography.AES_decode(signature, secretKey);
        } catch (Exception e) {
            throw new SecurityException("Invalid time signature.", e);
        }

        long timestamp;

        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw new SecurityException("Invalid timestamp format.", e);
        }

        long now = clock.millis();
        long expirationTime = expirationMin * 60_000L;
        long allowedClockSkew = allowedClockSkewSeconds * 1_000L;
        long earliestTimestamp = saturatedSubtract(now, expirationTime);
        long latestTimestamp = saturatedAdd(now, allowedClockSkew);

        return timestamp > earliestTimestamp && timestamp <= latestTimestamp;
    }

    /**
     * Adds two non-negative duration operands without overflowing a timestamp.
     *
     * @param value     the timestamp
     * @param increment the non-negative increment
     * @return the sum, or {@link Long#MAX_VALUE} when it would overflow
     */
    static long saturatedAdd(long value, long increment) {
        if (value > Long.MAX_VALUE - increment)
            return Long.MAX_VALUE;

        return value + increment;
    }

    /**
     * Subtracts a non-negative duration without overflowing a timestamp.
     *
     * @param value     the timestamp
     * @param decrement the non-negative decrement
     * @return the difference, or {@link Long#MIN_VALUE} when it would overflow
     */
    static long saturatedSubtract(long value, long decrement) {
        if (value < Long.MIN_VALUE + decrement)
            return Long.MIN_VALUE;

        return value - decrement;
    }

    /**
     * Generate the signature.
     *
     * @param timestamp The time stamp
     * @return signature
     */
    public String generateSignature(long timestamp) {
        if (ObjectHelper.isEmptyText(secretKey))
            throw new IllegalArgumentException("The secretKey is not set.");

        String timestampStr = String.valueOf(timestamp);

        return Cryptography.AES_encode(timestampStr, secretKey);
    }

    /**
     * Generate the signature.
     *
     * @return signature
     */
    public String generateSignature() {
        return generateSignature(clock.millis());
    }

    /**
     * Executes the action operation.
     *
     * @param annotation the annotation parameter.
     * @param req        the req parameter.
     * @return the operation result.
     */
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
