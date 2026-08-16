package com.ajaxjs.message.model.scheme;

import com.ajaxjs.message.model.enumration.SchemeValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 方案字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchemeFieldVO {
    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段key
     */
    private String key;

    /**
     * 字段描述
     */
    private String description;

    /**
     * 字段类型
     */
    private SchemeValueType type;
//    /**
//     * 选项（如果是选择型字段）
//     */
//    private List<IdStrAndName> options = new ArrayList<>();

    /**
     * 多对象字段
     */
    private List<MultiObjFieldVO> multiObjFields = new ArrayList<>();
}
