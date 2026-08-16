package com.ajaxjs.message.model.message.dingtalk.corp;

import com.ajaxjs.message.model.BaseMessage;
import com.ajaxjs.message.model.enumration.SchemeValueType;
import com.ajaxjs.message.model.scheme.SchemeValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 钉钉链接消息
 */
@EqualsAndHashCode(callSuper = true)
@Data

@NoArgsConstructor
@AllArgsConstructor
public class LinkMessageDTO extends BaseMessage {
    /**
     * 接收人分组列表
     */
    @SchemeValue(type = SchemeValueType.RECEIVER_GROUP)
    private List<Long> receiverGroupIds;

    @SchemeValue("是否发送给企业全部用户，注意钉钉限制只能发3次全员消息")
    private boolean toAllUser;

    /**
     * 接收人列表
     */
    @SchemeValue(type = SchemeValueType.RECEIVER)
    private List<String> receiverIds;

    @SchemeValue("接收人的部门id列表，接收者的部门id列表，多个用,隔开")
    private String deptIdList;

    @SchemeValue("消息点击链接地址，当发送消息为小程序时支持小程序跳转链接。")
    private String messageUrl;

    @SchemeValue("图片地址，可以通过上传媒体文件接口获取。")
    private String picUrl;

    @SchemeValue("消息标题，建议100字符以内。")
    private String title;

    @SchemeValue(type = SchemeValueType.TEXTAREA, value = "消息描述，建议500字符以内。")
    private String text;

}
