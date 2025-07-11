package com.ajaxjs.message.email;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class Email {
    /**
     * 发件人账号
     */
    private String account;

    /**
     * 发件人邮箱
     */
    private String from;

    /**
     * 收件人邮箱
     */
    private String to;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String content;

    /**
     * 邮件内容是否为 HTML 格式
     */
    private boolean isHtmlBody;

    /**
     * 附件列表，key 是文件名，byte[] 是文件内容
     */
    private Map<String, byte[]> attachment;
}
