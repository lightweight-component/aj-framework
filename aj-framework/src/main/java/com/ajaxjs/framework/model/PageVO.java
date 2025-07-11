package com.ajaxjs.framework.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class PageVO<T> {
    private List<T> list;

    /**
     * 总记录数
     */
    private int totalCount;
}
