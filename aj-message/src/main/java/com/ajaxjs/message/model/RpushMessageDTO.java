package com.ajaxjs.message.model;

import com.ajaxjs.message.model.enumration.SchemeValueType;
import com.ajaxjs.message.model.scheme.SchemeValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Socket消息发送DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RpushMessageDTO extends BaseMessage {
    /**
     * 消息来源，默认为系统消息，即-1
     */
    private long fromTo = -1;

    /**
     * 接收人列表
     */
    @SchemeValue(type = SchemeValueType.RECEIVER)
    private List<String> receiverIds;

    @SchemeValue(type = SchemeValueType.TEXTAREA, description = "请输入消息内容...")
    private String content;
}
