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
 * 企业微信图文消息发送DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data

@NoArgsConstructor
@AllArgsConstructor
public class NewsMessageDTO extends BaseMessage {
    /**
     * 接收人分组列表
     */
    @SchemeValue(type = SchemeValueType.RECEIVER_GROUP)
    private List<Long> receiverGroupIds;

    /**
     * 接收人列表
     */
    @SchemeValue(type = SchemeValueType.RECEIVER)
    private List<String> receiverIds;

    @SchemeValue(type = SchemeValueType.MULTI_OBJ_INPUT, value = "图文消息，一个图文消息支持1到8条图文")
    private List<ArticleDTO> articles;
}
