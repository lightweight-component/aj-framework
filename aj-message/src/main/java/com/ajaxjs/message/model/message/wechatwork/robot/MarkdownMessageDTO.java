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
 * 企业微信Markdown
 */
@EqualsAndHashCode(callSuper = true)
@Data

@NoArgsConstructor
@AllArgsConstructor
public class MarkdownMessageDTO extends BaseMessage {
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

    @SchemeValue(type = SchemeValueType.TEXTAREA, description = "请输入Markdown内容...")
    private String content;
}
