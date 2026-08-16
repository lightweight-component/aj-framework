package com.ajaxjs.message.model.message.wechatofficialaccount;

import com.ajaxjs.message.model.BaseMessage;
import com.ajaxjs.message.model.enumration.SchemeValueType;
import com.ajaxjs.message.model.scheme.SchemeValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 微信公众号图文消息发送DTO
 */
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

    @SchemeValue(value = "标题")
    private String title;

    @SchemeValue(value = "描述", description = "描述，超过512个字节，超过会自动截断")
    private String description;

    @SchemeValue(value = "点击跳转链接")
    private String url;

    @SchemeValue(value = "图片链接", description = "图片链接，图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图1068*455，小图150*150。")
    private String picUrl;

}
