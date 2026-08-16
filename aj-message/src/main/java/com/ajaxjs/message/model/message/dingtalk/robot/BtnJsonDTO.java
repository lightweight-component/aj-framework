package com.ajaxjs.message.model.message.dingtalk.robot;

import com.ajaxjs.message.model.scheme.MultiObjField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按钮
 *
 * @author 钟宝林
 * @since 2021/4/11/011 14:59
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor

public class BtnJsonDTO {
    @MultiObjField("按钮标题")
    private String title;

    @MultiObjField("点击按钮触发的URL")
    private String actionURL;
}
