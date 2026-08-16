package com.ajaxjs.message.model.message.wechatwork.robot;

import com.ajaxjs.message.model.scheme.MultiObjField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图文消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ArticleDTO {
    @MultiObjField(value = "标题")
    private String title;

    @MultiObjField(value = "描述", description = "超过512个字节，超过会自动截断")
    private String description;

    @MultiObjField(value = "点击跳转链接")
    private String url;

    @MultiObjField(value = "图片链接", description = "图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图1068*455，小图150*150。")
    private String picurl;
}
