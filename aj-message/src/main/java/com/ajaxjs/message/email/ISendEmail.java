package com.ajaxjs.message.email;

public interface ISendEmail {
    /**
     * Send a email
     *
     * @param email Email
     * @return If it's ok
     */
    boolean sendEmail(Email email);
}
