package com.ajaxjs.message.model.message.wechatofficialaccount;

import com.ajaxjs.message.model.scheme.MultiObjField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信模板Data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class WechatTemplateData {
    private String name;
    private String value;

    @MultiObjField(value = "显示颜色")
    private String color;
}
