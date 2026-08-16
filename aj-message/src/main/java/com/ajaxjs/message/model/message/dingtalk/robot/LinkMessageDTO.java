package com.ajaxjs.message.model.message.dingtalk.robot;

import com.ajaxjs.message.model.BaseMessage;
import com.ajaxjs.message.model.enumration.SchemeValueType;
import com.ajaxjs.message.model.scheme.SchemeValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 钉钉群消息link类型DTO
 *
 * @author 钟宝林
 **/
@EqualsAndHashCode(callSuper = true)
@Data

@NoArgsConstructor
@AllArgsConstructor
public class LinkMessageDTO extends BaseMessage {
    private static final long serialVersionUID = -3289428483627765265L;

    /**
     * 接收人分组列表
     */
    @SchemeValue(type = SchemeValueType.RECEIVER_GROUP)
    private List<Long> receiverGroupIds;

    @SchemeValue("是否@所有人")
    private boolean isAtAll;

    /**
     * 接收人列表
     */
    @SchemeValue(type = SchemeValueType.RECEIVER)
    private List<String> receiverIds;

    @SchemeValue(description = "消息标题")
    private String title;

    @SchemeValue(type = SchemeValueType.TEXTAREA, description = "消息内容。如果太长只会部分展示。")
    private String text;

    @SchemeValue(description = "点击消息跳转的URL")
    private String messageUrl;

    @SchemeValue(description = "图片URL")
    private String picUrl;

}
