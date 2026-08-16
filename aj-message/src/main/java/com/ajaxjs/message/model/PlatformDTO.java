package com.ajaxjs.message.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformDTO {
    private String id;

    /**
     * 平台中文名称
     */
    private String name;

    private String description;

    /**
     * 格式校验用的正则表达式
     */
    private String validateReg;

    /**
     * 是否启用
     */
    private boolean enable;
}
