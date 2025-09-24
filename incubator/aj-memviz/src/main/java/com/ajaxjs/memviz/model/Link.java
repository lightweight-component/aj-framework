package com.ajaxjs.memviz.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Link {
    public String source;
    public String target;
    public String field;   // 通过哪个字段/元素引用
}