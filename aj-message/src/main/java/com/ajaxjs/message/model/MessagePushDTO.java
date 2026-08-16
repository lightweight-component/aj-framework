package com.ajaxjs.message.model;

import com.ajaxjs.message.model.enumration.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 消息推送参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MessagePushDTO extends BaseMessage {
    /**
     * 消息参数，键为需要发送的消息类型，值为对应消息类型需要的参数（不同平台可能会需要不同的参数，所以这里不表达具体类型，由不同的实现决定具体结构）
     */
    private Map<MessageType, TypeMessageDTO> messageParam = new LinkedHashMap<>();
}
