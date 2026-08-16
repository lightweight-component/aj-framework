package com.ajaxjs.message.model.message.wechatwork.robot;

import com.ajaxjs.message.model.BaseMessage;
import com.ajaxjs.message.model.enumration.SchemeValueType;
import com.ajaxjs.message.model.scheme.SchemeValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 企业微信消息发送DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor

public class TextMessageDTO extends BaseMessage {
    /**
     * 接收人分组列表
     */
    @SchemeValue(type = SchemeValueType.RECEIVER_GROUP)
    private List<Long> receiverGroupIds;

    /**
     * 接收人列表
     */
    @SchemeValue(type = SchemeValueType.RECEIVER, description = "可以是手机号也可以是userId，如果要@所有人，就填all")
    private List<String> receiverIds;

    @SchemeValue(type = SchemeValueType.TEXTAREA, description = "请输入内容...")
    private String content;

}
