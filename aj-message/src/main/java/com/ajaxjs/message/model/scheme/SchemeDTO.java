package com.ajaxjs.message.model.scheme;

import com.ajaxjs.message.model.enumration.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 方案
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchemeDTO {
    private Long id;

    /**
     * 消息类型
     */
    private MessageType messageType;

    /**
     * 方案名称
     */
    private String name;

    /**
     * 参数
     */
    private String param;
}
