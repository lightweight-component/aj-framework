package com.ajaxjs.devtools.jmxmonitor.jvm.model;

import lombok.Data;

import java.util.List;

/**
 * Node
 */
@Data
public class Node {
    private String key;

    private String fullName;

    private String title;

    private String nodeType;

    private List<Node> children;
}

