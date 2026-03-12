package com.ajaxjs.security.captcha.image;

@FunctionalInterface
public interface SaveToRam {
    void save(String key, String value, int expireSeconds);
}
