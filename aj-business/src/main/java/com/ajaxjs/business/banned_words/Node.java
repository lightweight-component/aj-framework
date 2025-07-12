package com.ajaxjs.business.banned_words;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Node {
    private final Map<String, Node> children = new HashMap<>(0);
    private boolean isEnd = false;
    private String word;
    private double level = 0;

    public Node addChar(char c) {
        String cStr = String.valueOf(c);
        Node node = children.get(cStr);

        if (node == null) {
            node = new Node();
            children.put(cStr, node);
        }

        return node;
    }

    public Node findChar(char c) {
        return children.get(String.valueOf(c));
    }
}
