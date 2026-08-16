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
 * 微信公众号模板消息
 */
@EqualsAndHashCode(callSuper = true)
@Data

@NoArgsConstructor
@AllArgsConstructor
public class TemplateMessageDTO extends BaseMessage {
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

    @SchemeValue("公众号模板id")
    private String wechatTemplateId;

    @SchemeValue("点击跳转链接")
    private String url;

    @SchemeValue("小程序appId")
    private String miniAppId;

    @SchemeValue("小程序页面路径")
    private String miniPagePath;

    @SchemeValue(type = SchemeValueType.MULTI_OBJ_INPUT, value = "模板变量")
    private List<WechatTemplateData> templateDataList;
}
