package com.ajaxjs.devtools.jmxmonitor.jvm.model;

/**
 * NodeType
 */
public enum NodeType {

    DOMAIN("domain", 1),

    OBJECTNAME("objectName", 10);

    NodeType(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public static String getName(int value) {
        for (NodeType c : NodeType.values()) {
            if (c.getValue() == value)
                return c.name;
        }

        return null;
    }

    private String name;

    private int value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
