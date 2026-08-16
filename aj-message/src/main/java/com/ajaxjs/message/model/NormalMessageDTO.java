package com.ajaxjs.message.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 普通消息结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormalMessageDTO {
    /**
     * 发送方registrationId
     */
    private Long fromTo;

    /**
     * 接收方registrationId
     */
    private Long sendTo;

    private String content;
}
