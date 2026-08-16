package com.ajaxjs.message.model.message.wechatwork.agent;

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
 *
 * @author 钟宝林
 * @since 2021/4/7/007 17:30
 **/
@EqualsAndHashCode(callSuper = true)
@Data

@NoArgsConstructor
@AllArgsConstructor
public class NewsMessageDTO extends BaseMessage {
    private static final long serialVersionUID = 7034106110120563906L;

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

    @SchemeValue(description = "PartyID列表，非必填，多个接受者用‘|’分隔。当touser为@all时忽略本参数")
    private String toParty;

    @SchemeValue(description = "TagID列表，非必填，多个接受者用‘|’分隔。当touser为@all时忽略本参数")
    private String toTag;

    @SchemeValue(type = SchemeValueType.MULTI_OBJ_INPUT, value = "图文消息，一个图文消息支持1到8条图文")
    private List<ArticleDTO> articles;

}
