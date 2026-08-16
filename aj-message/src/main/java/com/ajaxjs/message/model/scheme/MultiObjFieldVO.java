package com.ajaxjs.message.model.scheme;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 多对象输入
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiObjFieldVO {
    private String key;
    private String label;
    private String description;
}
