package com.ajaxjs.message.model.message.dingtalk.corp;

import com.ajaxjs.message.model.scheme.MultiObjField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按钮
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class BtnJsonDTO {
    @MultiObjField("按钮标题")
    private String title;

    @MultiObjField("跳转链接")
    private String actionUrl;
}
