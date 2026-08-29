package com.ajaxjs.security.captcha.image;

/**
 * Represents the save to ram component.
 */
@FunctionalInterface
public interface SaveToRam {
    /**
     * Stores a captcha value in memory.
     *
     * @param key           the storage key
     * @param value         the captcha value
     * @param expireSeconds the expiration time in seconds
     */
    void save(String key, String value, int expireSeconds);
}
