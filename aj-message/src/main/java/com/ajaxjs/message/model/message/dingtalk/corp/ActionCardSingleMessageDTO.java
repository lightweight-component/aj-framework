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
 * 钉钉卡片消息-整体跳转
 */
@EqualsAndHashCode(callSuper = true)
@Data

@NoArgsConstructor
@AllArgsConstructor
public class ActionCardSingleMessageDTO extends BaseMessage {
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

    @SchemeValue("标题")
    private String singleTitle;

    @SchemeValue("跳转链接")
    private String singleUrl;

    @SchemeValue(type = SchemeValueType.TEXTAREA, description = "消息内容，支持markdown，语法参考标准markdown语法。建议1000个字符以内。")
    private String markdown;
}
